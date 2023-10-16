package hajime.vectormodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.axiIO.AXI4liteIO
import hajime.common._
import hajime.publicmodules._
import hajime.simple4Stage._
import chisel3.experimental.BundleLiterals._

class VectorCpu(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants with VectorOpConstants {
  import VEU_FUN._
  require(params.useVector, "fuck")
  val io = IO(new CPUIO())
  io := DontCare

  // fence, ecall, mretがEX、WBに存在する
  // ecallやmretがWBステージにあり，ジャンプ先PCがあるならばreadyを上げるが，IDにある命令は受け取らない
  val sysInst_in_pipeline = WireInit(false.B)

  // Modules
  val decoder = Module(new Decoder())
  val branch_predictor = Module(new BranchPredictor())
  val rf = Module(new RegFile())
  rf.io := DontCare
  val alu = Module(new ALU())
  alu.io := DontCare
  val branch_evaluator = Module(new BranchEvaluator())
  branch_evaluator.io := DontCare
  val bypassingUnit = Module(new BypassingUnit())
  bypassingUnit.io := DontCare
  val vectorLdstUnit = Module(new VectorLdstUnit())
  vectorLdstUnit.io := DontCare
  val csrUnit = Module(new CSRUnit())
  csrUnit.io := DontCare
  val multiplier = if (params.useMulDiv) Some(Module(new NonPipelinedMultiplierWrap())) else None
  val vectorDecoder = Module(new VectorDecoder())
  val vecCtrlUnit = Module(new VecCtrlUnit())
  val vecRegFile = Module(new VecRegFile(vrfPortNum = params.vecAluExecUnitNum + 1))
  val vrfReadyTable = Module(new VrfReadyTable(vrfPortNum = params.vecAluExecUnitNum + 1))
  val vecAluExecUnit = (0 until params.vecAluExecUnitNum).map(_ => Module(new IntegerAluExecUnit()))

  if (params.useMulDiv) multiplier.get.io := DontCare

  vectorLdstUnit.io.dcache <> io.dcache_axi4lite

  val cpu_operating = RegInit(false.B)
  when(!reset.asBool) {
    cpu_operating := true.B
  }

  val EX_stall = WireInit(false.B)
  val WB_stall = WireInit(false.B)
  val ID_stall = WireInit(false.B)
  val ID_flush = WireInit(false.B)
  val EX_flush = WireInit(false.B)
  // update PC in WB stage for ecall, mret or exception
  val WB_pc_redirect = WireInit(false.B)
  val rs1_required_but_not_valid = WireInit(false.B)
  val rs2_required_but_not_valid = WireInit(false.B)

  val decoded_inst = Wire(new InstBundle())
  decoded_inst := io.frontend.resp.bits.inst
  val ID_EX_REG = Reg(Valid(new ID_EX_IO()))
  val EX_WB_REG = Reg(Valid(new EX_WB_IO()))

  // これらがtrueならばベクトル命令を発行できる
  val vs1NonRequiredOrReady = !vrfReadyTable.io.vs1Check.valid || vrfReadyTable.io.vs1Check.ready
  val vs2NonRequiredOrReady = !vrfReadyTable.io.vs2Check.valid || vrfReadyTable.io.vs2Check.ready
  val vdNonRequiredOrReady = !vrfReadyTable.io.vdCheck.valid || vrfReadyTable.io.vdCheck.ready
  val vmNonRequiredOrReady = !vrfReadyTable.io.vmCheck.valid || vrfReadyTable.io.vmCheck.ready
  // 使用するベクトル機能ユニットが空いているか否か
  val vecLdstUnitReady = vectorLdstUnit.io.signalIn.ready
  val vecAluExecUnitReady = vecAluExecUnit.map(_.io.signalIn.ready)
  // ベクトル命令がvecLdstUnitを使うか否か
  val useVecLdstUnit = decoder.io.out.valid && decoder.io.out.bits.vector.get && vectorDecoder.io.out.useVecLdstExec && io.frontend.req.valid
  // vecLdstUnitを使わない，または（利用する必要がある時に）利用可能
  val vecLdstUnitNonRequiredOrReady = !useVecLdstUnit || vecLdstUnitReady
  // ベクトル命令がvecAluExecUnitを使うか否か
  val useVecAluExecUnit = decoder.io.out.valid && decoder.io.out.bits.vector.get && vectorDecoder.io.out.useVecAluExec && io.frontend.req.valid
  val vecAluExecUnitNonRequiredOrReady = !useVecAluExecUnit || vecAluExecUnitReady.reduce(_ || _)
  // 使用するオペランドで1つでもfalseのものがある，または使用するベクトル機能ユニットが利用できないならばベクトル命令は発行できない
  val vectorInstStall = !(vs1NonRequiredOrReady && vs2NonRequiredOrReady && vdNonRequiredOrReady && vmNonRequiredOrReady && vecLdstUnitNonRequiredOrReady && vecAluExecUnitNonRequiredOrReady)

  // メモリアクセス命令であればldstUnitがreadyである必要があり，
  // 乗算命令であればmultiplier.respがvalidである必要がある
  // vsetvl系でないベクタ命令ならば最終要素の実行である必要がある
  val vecExecUnitsReady: Seq[Bool] = Seq(vectorLdstUnit.io.signalIn.ready) ++ vecAluExecUnit.map(_.io.signalIn.ready)
  if (params.debug) assert(vecExecUnitsReady.map(_.asUInt).reduce(_ +& _) <= 1.U, s"Multiple VecUnits last Error.")

  val vecExecUnitHasInstButNotRetire: Seq[Bool] = (vecExecUnitsReady.map(!_) zip (Seq(!vectorLdstUnit.io.toExWbReg.valid) ++ vecAluExecUnit.map(!_.io.toExWbReg.valid))).map {
    case (notReady, notRetire) => notReady && notRetire
  }

  // START OF ID STAGE
  // TODO: 可読性向上のため，validのみのブロックとvalid && readyのブロックに分ける．EX，WBも同様

  // EXステージがvalidであり，かつEXステージが破棄できない場合，またはIDステージで必要なレジスタ値を取得できない場合，またはfence命令がある場合にreadyを下げる
  // ベクトル命令がEXに存在し，そのベクトル命令がリタイアしない，かつIDがスカラ命令・発行できないベクトル命令ならば同じくストール
  // vecExecUnitのうちreadyでないものが存在する
  val vecInstInEx = !vecExecUnitsReady.reduce(_ && _)
  // flushならばストールさせる必要はない
  ID_stall := !ID_flush && ((ID_EX_REG.valid && EX_stall) || rs1_required_but_not_valid || rs2_required_but_not_valid || sysInst_in_pipeline || vectorInstStall
    || (vecInstInEx && decoder.io.out.valid && (!decoder.io.out.bits.vector.get || vectorDecoder.io.out.isConfsetInst)))
  io.frontend.resp.ready := cpu_operating && !ID_stall

  io.frontend.req := Mux(branch_evaluator.io.out.valid && ID_EX_REG.valid, branch_evaluator.io.out, branch_predictor.io.out)
  io.frontend.req.valid := WB_pc_redirect || (branch_evaluator.io.out.valid && ID_EX_REG.valid) || (branch_predictor.io.out.valid && io.frontend.resp.valid && io.frontend.resp.ready)
  branch_predictor.io.pc := io.frontend.resp.bits.pc
  branch_predictor.io.imm := Mux(decoder.io.out.bits.isCondBranch, decoded_inst.b_imm, decoded_inst.j_imm)
  branch_predictor.io.BranchType := decoder.io.out.bits.branch

  decoder.io.inst := decoded_inst
  rf.io.rs1 := decoded_inst.rs1
  rf.io.rs2 := decoded_inst.rs2

  val ID_inst_valid = io.frontend.resp.valid && io.frontend.resp.ready
  val ID_fetchException = io.frontend.resp.bits.exceptionSignals.valid && ID_inst_valid
  val ID_illegal_instruction = !decoder.io.out.valid && ID_inst_valid
  val ID_ecall = decoder.io.out.valid && (decoder.io.out.bits.branch === Branch.ECALL.asUInt) && ID_inst_valid
  val ID_exception = ID_fetchException || ID_illegal_instruction || ID_ecall

  ID_EX_REG.valid := ID_inst_valid
  ID_EX_REG.bits.exceptionSignals.valid := ID_exception
  ID_EX_REG.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    ID_fetchException -> io.frontend.resp.bits.exceptionSignals.bits,
    ID_illegal_instruction -> Causes.illegal_instruction.U,
    ID_ecall -> Causes.machine_ecall.U,
  ))
  ID_EX_REG.bits.dataSignals.pc := io.frontend.resp.bits.pc
  ID_EX_REG.bits.dataSignals.bp_destPC := branch_predictor.io.out.bits.pc
  ID_EX_REG.bits.dataSignals.bp_taken := branch_predictor.io.out.valid
  ID_EX_REG.bits.dataSignals.imm := MuxCase(0.U, Seq(
    (decoder.io.out.bits.value1 === Value1.U_IMM.asUInt) -> decoded_inst.u_imm,
    (decoder.io.out.bits.value1 === Value1.UIMM19_15.asUInt) -> decoded_inst.uimm,
    (decoder.io.out.bits.value2 === Value2.I_IMM.asUInt) -> decoded_inst.i_imm,
    (decoder.io.out.bits.value2 === Value2.S_IMM.asUInt) -> decoded_inst.s_imm,
  ))

  val rs1ValueToEX = Mux(bypassingUnit.io.ID.out.rs1_value.valid, bypassingUnit.io.ID.out.rs1_value.bits, rf.io.rs1_out)
  val rs2ValueToEX = Mux(bypassingUnit.io.ID.out.rs2_value.valid, bypassingUnit.io.ID.out.rs2_value.bits, rf.io.rs2_out)

  ID_EX_REG.bits.dataSignals.rs1 := rs1ValueToEX
  ID_EX_REG.bits.dataSignals.rs2 := rs2ValueToEX
  ID_EX_REG.bits.dataSignals.zimm := decoded_inst.zimm
  ID_EX_REG.bits.ctrlSignals.decode := decoder.io.out.bits
  ID_EX_REG.bits.ctrlSignals.rd_index := decoded_inst.rd

  // ベクトル命令を追加
  bypassingUnit.io.ID.in.rs1_index.bits := decoded_inst.rs1
  // exceptionの際にこれを下げる必要があるかもしれない
  bypassingUnit.io.ID.in.rs1_index.valid := decoder.io.out.bits.use_RS1 && decoder.io.out.valid && io.frontend.resp.valid
  bypassingUnit.io.ID.in.rs2_index.bits := decoded_inst.rs2
  bypassingUnit.io.ID.in.rs2_index.valid := decoder.io.out.bits.use_RS2 && decoder.io.out.valid && io.frontend.resp.valid

  rs1_required_but_not_valid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs1_bypassMatchAtEX -> (!bypassingUnit.io.EX.in.bits.rd.valid),
    bypassingUnit.io.ID.out.rs1_bypassMatchAtWB -> (!bypassingUnit.io.WB.in.bits.rd.valid),
  ))
  rs2_required_but_not_valid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs2_bypassMatchAtEX -> (!bypassingUnit.io.EX.in.bits.rd.valid),
    bypassingUnit.io.ID.out.rs2_bypassMatchAtWB -> (!bypassingUnit.io.WB.in.bits.rd.valid),
  ))

  val vecConfBypass = Wire(new VecCtrlUnitResp())
  val vtypeBypass = vecConfBypass.vtype
  val vlBypass = vecConfBypass.vl

  vecConfBypass := Mux(vecCtrlUnit.io.resp.valid, vecCtrlUnit.io.resp.bits, EX_WB_REG.bits.vectorCsrPorts.get)

  vectorDecoder.io.inst := decoded_inst

  // ベクトル命令がベクトル設定・メモリアクセスでなく，かつvvならばvs1を使用する
  vrfReadyTable.io.vs1Check.valid := decoder.io.out.valid && decoder.io.out.bits.vector.get && !(vectorDecoder.io.out.avl_sel === AVL_SEL.NONE.asUInt) && vectorDecoder.io.out.mop === MOP.NONE.asUInt && (vectorDecoder.io.out.vSource === VSOURCE.VV.asUInt)
  vrfReadyTable.io.vs1Check.bits.idx := decoded_inst.rs1
  vrfReadyTable.io.vs1Check.bits.vtype := vtypeBypass
  vrfReadyTable.io.vs1Check.bits.vm := vectorDecoder.io.out.veuFun.isMaskInst
  // ベクトル設定命令でなく，かつメモリアクセスでないまたはインデックスならばvs2を使用する
  vrfReadyTable.io.vs2Check.valid := decoder.io.out.valid && decoder.io.out.bits.vector.get && !(vectorDecoder.io.out.avl_sel === AVL_SEL.NONE.asUInt) && ((vectorDecoder.io.out.mop === MOP.NONE.asUInt) || (vectorDecoder.io.out.mop === MOP.IDX_ORDERED.asUInt))
  vrfReadyTable.io.vs2Check.bits.idx := decoded_inst.rs2
  vrfReadyTable.io.vs2Check.bits.vtype := vtypeBypass
  vrfReadyTable.io.vs2Check.bits.vm := vectorDecoder.io.out.veuFun.isMaskInst
  // ベクトル設定命令でないならばvdを使用する
  vrfReadyTable.io.vdCheck.valid := decoder.io.out.valid && decoder.io.out.bits.vector.get && !(vectorDecoder.io.out.avl_sel === AVL_SEL.NONE.asUInt)
  vrfReadyTable.io.vdCheck.bits.idx := decoded_inst.rd
  vrfReadyTable.io.vdCheck.bits.vtype := vtypeBypass
  vrfReadyTable.io.vdCheck.bits.vm := vectorDecoder.io.out.veuFun.writeAsMask
  // vmフィールドが1ならばvmを使用する
  vrfReadyTable.io.vmCheck.valid := decoder.io.out.valid && decoder.io.out.bits.vector.get && !(vectorDecoder.io.out.avl_sel === AVL_SEL.NONE.asUInt) && !vectorDecoder.io.out.vm
  // ベクトル設定命令でなく，かつストア命令で無ければvdへ書き込む
  vrfReadyTable.io.invalidateVd := io.frontend.resp.valid && io.frontend.resp.ready && decoder.io.out.valid && decoder.io.out.bits.vector.get && (vectorDecoder.io.out.avl_sel =/= AVL_SEL.NONE.asUInt) && !decoder.io.out.bits.memWrite

  // vecAluExecUnitを使用するなら，空いている方をvalidにする
  when(io.frontend.resp.valid && decoder.io.out.valid && decoder.io.out.bits.vector.get && vectorDecoder.io.out.useVecAluExec) {
    val vecAluExecUnitAssigned = WireInit(false.B)
    for (x <- vecAluExecUnit) {
      when(x.io.signalIn.ready && !vecAluExecUnitAssigned) {
        x.io.signalIn.valid := true.B
        x.io.signalIn.bits := (new VectorExecUnitSignalIn()).Lit(
          _.vs1 -> decoded_inst.rs1,
          _.vs2 -> decoded_inst.rs2,
          _.vd -> decoded_inst.rd,
          _.scalarVal -> rs1ValueToEX,
          _.vectorDecode -> vectorDecoder.io.out,
          _.scalarDecode -> decoder.io.out.bits,
          _.scalarDecode.vector.get -> decoder.io.out.bits.vector.get,
          _.vecConf -> vecConfBypass,
          _.pc -> io.frontend.resp.bits.pc,
        )
        if(params.debug) {
          x.io.signalIn.bits.debug.get.instruction := decoded_inst.bits
          x.io.signalIn.bits.debug.get.pc := io.frontend.resp.bits.pc
        }
        vecAluExecUnitAssigned := true.B
      } .otherwise {
        x.io.signalIn := DontCare
        x.io.signalIn.valid := false.B
      }
    }
  }
  // vecLdstUnitに対しても同様
  when(io.frontend.resp.valid && decoder.io.out.valid && decoder.io.out.bits.vector.get && vectorDecoder.io.out.useVecLdstExec && vecLdstUnitReady) {
    vectorLdstUnit.io.signalIn.valid := true.B
    vectorLdstUnit.io.signalIn.bits.scalar.rs2Value := rs2ValueToEX
    vectorLdstUnit.io.signalIn.bits.scalar.immediate := Mux(decoder.io.out.bits.memRead, decoded_inst.i_imm, decoded_inst.s_imm)
    vectorLdstUnit.io.signalIn.bits.scalar.rdIndex := decoded_inst.rd
    vectorLdstUnit.io.signalIn.bits.vector := (new VectorExecUnitSignalIn()).Lit(
      _.vs1 -> decoded_inst.rs1,
      _.vs2 -> decoded_inst.rs2,
      _.vd -> decoded_inst.rd,
      _.scalarVal -> rs1ValueToEX,
      _.vectorDecode -> vectorDecoder.io.out,
      _.scalarDecode -> decoder.io.out,
      _.vecConf -> vecConfBypass,
      _.pc -> io.frontend.resp.bits.pc,
    )
    if(params.debug) {
      vectorLdstUnit.io.signalIn.bits.vector.debug.get := (new Debug_Info()).Lit(
      _.pc -> io.frontend.resp.bits.pc,
      _.instruction -> decoded_inst,
      )
    }
  } .otherwise {
    vectorLdstUnit.io.signalIn := DontCare
    vectorLdstUnit.io.signalIn.valid := false.B
  }

  when(decoder.io.out.valid && decoder.io.out.bits.vector.get) {
    ID_EX_REG.bits.vectorCtrlSignals.get := vectorDecoder.io.out
  }
  // 0 -> v0.mask[i]が1ならば書き込み，0ならば書き込まない
  // 1 -> マスクなし，全て書き込む
  // （マスクを使わないベクタ命令は全てvm=1か？）
  ID_EX_REG.bits.vectorDataSignals.get.mask := vectorDecoder.io.out.vm
  ID_EX_REG.bits.vectorDataSignals.get.vs1 := decoded_inst.rs1
  ID_EX_REG.bits.vectorDataSignals.get.vs2 := decoded_inst.rs2
  ID_EX_REG.bits.vectorDataSignals.get.vd := decoded_inst.rd

  if (params.debug) {
    ID_EX_REG.bits.debug.get.instruction := decoded_inst.bits
    ID_EX_REG.bits.debug.get.pc := io.frontend.resp.bits.pc
  }

  // retain the ID_EX register if stall
  when(EX_stall) {
    ID_EX_REG := ID_EX_REG
  }
  // flush the ID_EX register if branch miss, ecall, mret or exception
  ID_flush := EX_flush || branch_evaluator.io.out.valid
  when(ID_flush) {
    ID_EX_REG.valid := false.B
  }

  // START OF EX STAGE

  // vecRegFileのレジスタ読み出し
  vecRegFile.io.readReq(0) <> vectorLdstUnit.io.readVrf
  for((d,i) <- vecAluExecUnit.zipWithIndex) {
    vecRegFile.io.readReq(i + 1) <> d.io.readVrf
  }
  // vecRegFileへのレジスタ書き込み
  vecRegFile.io.writeReq(0) := vectorLdstUnit.io.vectorResp.toVRF
  vrfReadyTable.io.fromVecExecUnit(0) := vectorLdstUnit.io.vectorResp.toVRF
  for((d,i) <- vecAluExecUnit.zipWithIndex) {
    vecRegFile.io.writeReq(i + 1) := d.io.dataOut.toVRF
    vrfReadyTable.io.fromVecExecUnit(i + 1) := d.io.dataOut.toVRF
  }

  if (params.debug) {
    io.debug_io.get.vrfMap := vecRegFile.io.debug.get
  }

  alu.io.in1 := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.value1, 0.U)(Seq(
    Value1.RS1.asUInt -> ID_EX_REG.bits.dataSignals.rs1,
    Value1.U_IMM.asUInt -> ID_EX_REG.bits.dataSignals.imm,
  ))
  alu.io.in2 := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.value2, 0.U)(Seq(
    Value2.RS2.asUInt -> ID_EX_REG.bits.dataSignals.rs2,
    Value2.I_IMM.asUInt -> ID_EX_REG.bits.dataSignals.imm,
    Value2.S_IMM.asUInt -> ID_EX_REG.bits.dataSignals.imm,
    Value2.PC.asUInt -> ID_EX_REG.bits.dataSignals.pc.addr,
  ))
  alu.io.funct := ID_EX_REG.bits.ctrlSignals.decode

  branch_evaluator.io.req.bits.ALU_Result := alu.io.out
  branch_evaluator.io.req.bits.BranchType := ID_EX_REG.bits.ctrlSignals.decode.branch
  branch_evaluator.io.req.bits.destPC := ID_EX_REG.bits.dataSignals.bp_destPC
  branch_evaluator.io.req.bits.pc := ID_EX_REG.bits.dataSignals.pc
  branch_evaluator.io.req.bits.bp_taken := ID_EX_REG.bits.dataSignals.bp_taken
  branch_evaluator.io.req.valid := ID_EX_REG.valid

  if (params.useMulDiv) {
    val multiplier_hasValue = RegInit(false.B)
    // EXステージに有効な乗算命令があり，かつ乗算器の出力のvalidとreadyが共にtrueで無ければ乗算器に保持するべき情報（現在のEXステージの乗算命令）がある
    multiplier_hasValue := ID_EX_REG.bits.ctrlSignals.decode.use_MUL && ID_EX_REG.valid && !(multiplier.get.io.resp.ready && multiplier.get.io.resp.valid)
    multiplier.get.io.req.bits.rs1 := ID_EX_REG.bits.dataSignals.rs1
    multiplier.get.io.req.bits.rs2 := ID_EX_REG.bits.dataSignals.rs2
    multiplier.get.io.req.bits.funct := ID_EX_REG.bits.ctrlSignals.decode
    // 乗算器に保持するべき情報（現在のEXステージの乗算命令）があればvalidを下げる（乗算器が既に情報を受け取っているため）
    multiplier.get.io.req.valid := ID_EX_REG.bits.ctrlSignals.decode.use_MUL && !multiplier_hasValue && ID_EX_REG.valid && !EX_flush
    multiplier.get.io.resp.ready := !(EX_WB_REG.valid && WB_stall)
  }

  // TODO: rs1がx0かつrdが非x0の場合にvlを最大に，rs1・rdともにx0の場合にvlを変更しないように仕様変更
  vecCtrlUnit.io.req.valid := ID_EX_REG.valid && ID_EX_REG.bits.ctrlSignals.decode.vector.get && ID_EX_REG.bits.vectorCtrlSignals.get.isConfsetInst
  vecCtrlUnit.io.req.bits.vDecode := ID_EX_REG.bits.vectorCtrlSignals.get
  vecCtrlUnit.io.req.bits.rs1_value := ID_EX_REG.bits.dataSignals.rs1
  vecCtrlUnit.io.req.bits.rs2_value := ID_EX_REG.bits.dataSignals.rs2
  vecCtrlUnit.io.req.bits.zimm := ID_EX_REG.bits.dataSignals.zimm
  vecCtrlUnit.io.req.bits.uimm := ID_EX_REG.bits.dataSignals.imm

  val exScalarRes = if (params.useMulDiv) {
    Mux(ID_EX_REG.bits.ctrlSignals.decode.use_MUL, multiplier.get.io.resp.bits, alu.io.out)
  } else {
    alu.io.out
  }

  // placefolder for vec->scalar inst (vcpop.m, vfirst.m, vmv.x.s)
  val exVectorRes = vecCtrlUnit.io.resp.bits.vl

  bypassingUnit.io.EX.in.bits.rd.bits.index := ID_EX_REG.bits.ctrlSignals.rd_index
  bypassingUnit.io.EX.in.bits.rd.bits.value := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.writeback_selector, 0.U)(Seq(
    WB_SEL.PC4.asUInt -> ID_EX_REG.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH.asUInt -> exScalarRes,
    WB_SEL.VECTOR.asUInt -> exVectorRes,
  ))
  bypassingUnit.io.EX.in.bits.rd.valid := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.writeback_selector, false.B)(Seq(
    WB_SEL.PC4.asUInt -> true.B,
    WB_SEL.ARITH.asUInt -> (if (params.useMulDiv) !ID_EX_REG.bits.ctrlSignals.decode.use_MUL || multiplier.get.io.resp.valid else true.B),
    WB_SEL.CSR.asUInt -> false.B,
    WB_SEL.MEM.asUInt -> false.B,
    WB_SEL.NONE.asUInt -> false.B,
    WB_SEL.VECTOR.asUInt -> (if (params.useVector) true.B else false.B)
  )) && ID_EX_REG.valid
  bypassingUnit.io.EX.in.valid := ID_EX_REG.bits.ctrlSignals.decode.write_to_rd && ID_EX_REG.valid

  EX_WB_REG.valid := ID_EX_REG.valid && (!ID_EX_REG.bits.ctrlSignals.decode.memValid || vectorLdstUnit.io.signalIn.ready) &&
    (if (params.useMulDiv) !ID_EX_REG.bits.ctrlSignals.decode.use_MUL || multiplier.get.io.resp.valid else true.B) &&
    (!ID_EX_REG.bits.ctrlSignals.decode.vector.get || ID_EX_REG.bits.vectorCtrlSignals.get.isConfsetInst || vecExecUnitsReady.reduce(_ || _))
  EX_WB_REG.bits.dataSignals.pc := ID_EX_REG.bits.dataSignals.pc
  EX_WB_REG.bits.dataSignals.exResult := MuxLookup(ID_EX_REG.bits.ctrlSignals.decode.writeback_selector, 0.U)(Seq(
    WB_SEL.ARITH.asUInt -> exScalarRes,
    WB_SEL.VECTOR.asUInt -> exVectorRes,
  ))
  EX_WB_REG.bits.dataSignals.datatoCSR := Mux(ID_EX_REG.bits.ctrlSignals.decode.value1 === Value1.RS1.asUInt, ID_EX_REG.bits.dataSignals.rs1, ID_EX_REG.bits.dataSignals.imm)
  EX_WB_REG.bits.dataSignals.csr_addr := ID_EX_REG.bits.dataSignals.zimm

  EX_WB_REG.bits.ctrlSignals := ID_EX_REG.bits.ctrlSignals

  EX_WB_REG.bits.exceptionSignals.valid := (ID_EX_REG.valid && ID_EX_REG.bits.exceptionSignals.valid) || (ID_EX_REG.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt) && ID_EX_REG.valid
  // only machine-mode ecall and illegal inst is supported now
  EX_WB_REG.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    // if there is already exception before ID, then retain
    ID_EX_REG.bits.exceptionSignals.valid -> ID_EX_REG.bits.exceptionSignals.bits,
    // else if exception in EX (load/store misaligned or access fault),
  ))
  Mux(ID_EX_REG.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt, 0xb.U(params.xprlen.W), 0.U)

  when(vecCtrlUnit.io.resp.valid) {
    EX_WB_REG.bits.vectorCsrPorts.get := vecCtrlUnit.io.resp.bits
  }

  if (params.debug) EX_WB_REG.bits.debug.get := ID_EX_REG.bits.debug.get

  // WBステージがvalidかつ破棄できないかつEXステージに有効な値がある場合，またはメモリアクセス命令かつldstUnit.reqがreadyでない，または乗算命令で乗算器がvalidでない
  // またはベクタ命令実行完了前にスカラ命令がID_EXレジスタにある，またはチェイニング不可能なベクタ命令（構造ハザード・0要素目の値が用意できていないなど）
  EX_stall := ID_EX_REG.valid && ((EX_WB_REG.valid && WB_stall) || (ID_EX_REG.bits.ctrlSignals.decode.memValid && !vectorLdstUnit.io.signalIn.ready) || (if (params.useMulDiv) {
    ID_EX_REG.bits.ctrlSignals.decode.use_MUL && !multiplier.get.io.resp.valid
  } else false.B))
  // ベクトル命令がEXにある場合，IDがスカラ命令，またはIDのベクトル命令が発行できないならばIDの方でストールさせる

  // リタイアするベクトル命令があればそれでEX_WB_REGを上書き


  when(WB_stall) {
    EX_WB_REG := EX_WB_REG
  }

  // flush the EX_WB register if ecall, mret or exception
  EX_flush := WB_pc_redirect
  when(EX_flush) {
    EX_WB_REG.valid := false.B
  }

  // START OF WB STAGE
  // メモリアクセス命令かつ，ベクトル実行ユニットのベクトル命令がリタイアしない（toExWbRegがvalidでない）かつ，respがvalidでなければストール
  WB_stall := EX_WB_REG.valid && (EX_WB_REG.bits.ctrlSignals.decode.memValid && !vectorLdstUnit.io.vectorResp.toVRF.valid && !vectorLdstUnit.io.scalarResp.valid)
  val dmemoryAccessException = (EX_WB_REG.bits.ctrlSignals.decode.memValid && ldstUnit.io.cpu.resp.valid && ldstUnit.io.cpu.resp.bits.exceptionSignals.valid)
  WB_pc_redirect := EX_WB_REG.valid && (EX_WB_REG.bits.ctrlSignals.decode.branch === Branch.MRET.asUInt || EX_WB_REG.bits.exceptionSignals.valid || dmemoryAccessException)
  when(WB_pc_redirect) {
    io.frontend.req.bits.pc := csrUnit.io.resp.data
  }
  // 割り込みまたは例外の場合は、PCのみ更新しリタイアしない（命令を破棄）
  val WB_inst_can_retire = EX_WB_REG.valid && !(EX_WB_REG.bits.exceptionSignals.valid || dmemoryAccessException) && !WB_stall
  rf.io.req.valid := WB_inst_can_retire && EX_WB_REG.bits.ctrlSignals.decode.write_to_rd
  rf.io.req.bits.data := MuxLookup(EX_WB_REG.bits.ctrlSignals.decode.writeback_selector, 0.U)(Seq(
    WB_SEL.PC4 -> EX_WB_REG.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH -> EX_WB_REG.bits.dataSignals.exResult,
    WB_SEL.CSR -> csrUnit.io.resp.data,
    WB_SEL.MEM -> ldstUnit.io.cpu.resp.bits.data,
    WB_SEL.VECTOR -> (if (params.useVector) EX_WB_REG.bits.dataSignals.exResult else 0.U)
  ).map {
    case (wb_sel, data) => (wb_sel.asUInt, data)
  })
  rf.io.req.bits.rd := EX_WB_REG.bits.ctrlSignals.rd_index

  bypassingUnit.io.WB.in.bits.rd.valid := bypassingUnit.io.WB.in.valid && (!EX_WB_REG.bits.ctrlSignals.decode.memRead || ldstUnit.io.cpu.resp.valid)
  bypassingUnit.io.WB.in.bits.rd.bits.index := EX_WB_REG.bits.ctrlSignals.rd_index
  bypassingUnit.io.WB.in.bits.rd.bits.value := rf.io.req.bits.data
  bypassingUnit.io.WB.in.valid := EX_WB_REG.bits.ctrlSignals.decode.write_to_rd && WB_inst_can_retire

  csrUnit.io.req.valid := EX_WB_REG.valid
  // ecallやmretの処理はcsrUnit内で行われる
  csrUnit.io.req.bits.funct := EX_WB_REG.bits.ctrlSignals.decode
  csrUnit.io.req.bits.data := EX_WB_REG.bits.dataSignals.datatoCSR
  csrUnit.io.req.bits.csr_addr := EX_WB_REG.bits.dataSignals.csr_addr
  csrUnit.io.fromCPU.hartid := io.hartid
  csrUnit.io.fromCPU.cpu_operating := cpu_operating
  csrUnit.io.fromCPU.inst_retire := WB_inst_can_retire
  csrUnit.io.exception.valid := (EX_WB_REG.bits.exceptionSignals.valid || dmemoryAccessException) && EX_WB_REG.valid
  csrUnit.io.exception.bits.mepc_write := EX_WB_REG.bits.dataSignals.pc.addr
  csrUnit.io.exception.bits.mcause_write := Mux(dmemoryAccessException, ldstUnit.io.cpu.resp.bits.exceptionSignals.bits, EX_WB_REG.bits.exceptionSignals.bits)

  if (params.useVector) csrUnit.io.vectorCsrPorts.get := EX_WB_REG.bits.vectorCsrPorts.get

  // EXまたはWBステージにfence, ecall, mretがある
  sysInst_in_pipeline := (ID_EX_REG.valid && ID_EX_REG.bits.ctrlSignals.decode.isSysInst) || (EX_WB_REG.valid && EX_WB_REG.bits.ctrlSignals.decode.isSysInst)

  if (params.debug) {
    io.debug_io.get.debug_retired.bits.instruction.bits := EX_WB_REG.bits.debug.get.instruction & Fill(32, EX_WB_REG.valid)
    io.debug_io.get.debug_retired.bits.pc.addr := EX_WB_REG.bits.debug.get.pc.addr & Fill(params.xprlen, EX_WB_REG.valid)
    io.debug_io.get.debug_retired.valid := WB_inst_can_retire
    io.debug_io.get.debug_abi_map := rf.io.debug_abi_map.get
  }
}
