package hajime.vectormodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common._
import hajime.publicmodules._
import hajime.simple4Stage._
import chisel3.experimental.BundleLiterals._

class VectorCpu(implicit params: HajimeCoreParams) extends CpuModule with ScalarOpConstants with VectorOpConstants {
  import VEU_FUN._
  require(params.useVector, "fuck")
  // val io = IO(new CpuIo())
  io := DontCare

  // fence, ecall, mretがEX、WBに存在する
  // ecallやmretがWBステージにあり，ジャンプ先PCがあるならばreadyを上げるが，IDにある命令は受け取らない
  val sysInstInPipeline = WireInit(false.B)

  // Modules
  val decoder = Module(new Decoder())
  val branchPredictor = Module(new BranchPredictor())
  val rf = Module(new RegFile())
  rf.io := DontCare
  val alu = Module(new ALU())
  alu.io := DontCare
  val branchEvaluator = Module(new BranchEvaluator())
  branchEvaluator.io := DontCare
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

  vectorLdstUnit.io.dcache <> io.dCacheAxi4Lite

  val cpuOperating = RegInit(false.B)
  when(!reset.asBool) {
    cpuOperating := true.B
  }

  val exStall = WireInit(false.B)
  val wbStall = WireInit(false.B)
  val idStall = WireInit(false.B)
  val idFlush = WireInit(false.B)
  val exFlush = WireInit(false.B)
  // update PC in WB stage for ecall, mret or exception
  val wbPcRedirect = WireInit(false.B)
  val rs1RequiredButNotValid = WireInit(false.B)
  val rs2RequiredButNotValid = WireInit(false.B)

  val decodedInst = Wire(new InstBundle())
  decodedInst := io.frontend.resp.bits.inst
  val idExReg = RegInit(Valid(new idExIo()).Lit(
    _.valid -> false.B,
  ))
  val exWbReg = RegInit(Valid(new exWbIo()).Lit(
    _.valid -> false.B,
  ))

  // これらがtrueならばベクトル命令を発行できる
  val vs1NonRequiredOrReady = !vrfReadyTable.io.vs1Check.valid || vrfReadyTable.io.vs1Check.ready
  val vs2NonRequiredOrReady = !vrfReadyTable.io.vs2Check.valid || vrfReadyTable.io.vs2Check.ready
  val vdNonRequiredOrReady = !vrfReadyTable.io.vdCheck.valid || vrfReadyTable.io.vdCheck.ready
  val vmNonRequiredOrReady = !vrfReadyTable.io.vmCheck.valid || vrfReadyTable.io.vmCheck.ready
  // 使用するベクトル機能ユニットが空いているか否か
  val vecLdstUnitReady = vectorLdstUnit.io.signalIn.ready
  val vecAluExecUnitReady = vecAluExecUnit.map(_.io.signalIn.ready)
  // ベクトル命令がvecLdstUnitを使うか否か
  val useVecLdstUnit = decoder.io.out.valid && decoder.io.out.bits.vector.get && vectorDecoder.io.out.useVecLdstExec // && io.frontend.req.valid
  // vecLdstUnitを使わない，または（利用する必要がある時に）利用可能
  val vecLdstUnitNonRequiredOrReady = !useVecLdstUnit || vecLdstUnitReady
  // ベクトル命令がvecAluExecUnitを使うか否か
  val useVecAluExecUnit = decoder.io.out.valid && decoder.io.out.bits.vector.get && vectorDecoder.io.out.useVecAluExec // && io.frontend.req.valid
  val vecAluExecUnitNonRequiredOrReady = !useVecAluExecUnit || vecAluExecUnitReady.reduce(_ || _)
  // 使用するオペランドで1つでもfalseのものがある，または使用するベクトル機能ユニットが利用できないならばベクトル命令は発行できない
  val vectorInstStall = !(vs1NonRequiredOrReady && vs2NonRequiredOrReady && vdNonRequiredOrReady && vmNonRequiredOrReady && vecLdstUnitNonRequiredOrReady && vecAluExecUnitNonRequiredOrReady)

  // メモリアクセス命令であればldstUnitがreadyである必要があり，
  // 乗算命令であればmultiplier.respがvalidである必要がある
  // vsetvl系でないベクタ命令ならば最終要素の実行である必要がある
  val vecExecUnitsReady: Seq[Bool] = Seq(vectorLdstUnit.io.signalIn.ready) ++ vecAluExecUnit.map(_.io.signalIn.ready)
  val vecExecUnitsToExWbRegValid: Seq[Bool] = Seq(vectorLdstUnit.io.toExWbReg.valid) ++ vecAluExecUnit.map(_.io.toExWbReg.valid)
  if(params.debug) {
    when(vecExecUnitsToExWbRegValid.map(_.asUInt).reduce (_ +& _) > 1.U) {
      printf("Multiple VecUnits last Error.\n")
      printf("Debug Info:\n")
      val toExWbRegList: Seq[Valid[exWbIo]] = Seq(vectorLdstUnit.io.toExWbReg) ++ vecAluExecUnit.map(_.io.toExWbReg)
      toExWbRegList.zipWithIndex.foreach {
        case (d, i) => {
          printf("vecExecUnit%d:\n", i.U)
          printf("valid:%b\n", d.valid)
          printf("inst:%x\n", d.bits.debug.get.instruction)
          printf("pc:%x\n", d.bits.debug.get.pc.addr)
        }
      }
    }
  }

  /*
  // Not used
  val vecExecUnitHasInstButNotRetire: Seq[Bool] = (vecExecUnitsReady.map(!_) zip (Seq(!vectorLdstUnit.io.toExWbReg.valid) ++ vecAluExecUnit.map(!_.io.toExWbReg.valid))).map {
    case (notReady, notRetire) => notReady && notRetire
  }
   */

  // START OF ID STAGE
  // TODO: 可読性向上のため，validのみのブロックとvalid && readyのブロックに分ける．EX，WBも同様

  // EXステージがvalidであり，かつEXステージが破棄できない場合，またはIDステージで必要なレジスタ値を取得できない場合，またはfence命令がある場合にreadyを下げる
  // ベクトル命令がEXに存在し，そのベクトル命令がリタイアしない，かつIDがスカラ命令・発行できないベクトル命令ならば同じくストール
  // vecExecUnitのうちreadyでないものが存在する
  val vecInstInEx = !vecExecUnitsReady.reduce(_ && _)
  // flushならばストールさせる必要はない
  idStall := !idFlush && ((idExReg.valid && exStall) || rs1RequiredButNotValid || rs2RequiredButNotValid || sysInstInPipeline || vectorInstStall
    || (vecInstInEx && decoder.io.out.valid && (!decoder.io.out.bits.vector.get || vectorDecoder.io.out.isConfsetInst || vectorDecoder.io.out.vecPermutation)))
  io.frontend.resp.ready := cpuOperating && !idStall

  io.frontend.req := Mux(branchEvaluator.io.out.valid && idExReg.valid, branchEvaluator.io.out, branchPredictor.io.out)
  io.frontend.req.valid := wbPcRedirect || (branchEvaluator.io.out.valid && idExReg.valid) || (branchPredictor.io.out.valid && io.frontend.resp.valid && io.frontend.resp.ready)
  branchPredictor.io.pc := io.frontend.resp.bits.pc
  branchPredictor.io.imm := Mux(decoder.io.out.bits.isCondBranch, decodedInst.b_imm, decodedInst.j_imm)
  branchPredictor.io.BranchType := decoder.io.out.bits.branch

  decoder.io.inst := decodedInst
  rf.io.rs1 := decodedInst.rs1
  rf.io.rs2 := decodedInst.rs2

  val idInstValid = io.frontend.resp.valid && io.frontend.resp.ready
  val idFetchException = io.frontend.resp.bits.exceptionSignals.valid && idInstValid
  val idIllegalInstruction = !decoder.io.out.valid && idInstValid
  val idEcall = decoder.io.out.valid && (decoder.io.out.bits.branch === Branch.ECALL.asUInt) && idInstValid
  val idException = idFetchException || idIllegalInstruction || idEcall

  idExReg.valid := idInstValid
  idExReg.bits.exceptionSignals.valid := idException
  idExReg.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    idFetchException -> io.frontend.resp.bits.exceptionSignals.bits,
    idIllegalInstruction -> Causes.illegal_instruction.U,
    idEcall -> Causes.machine_ecall.U,
  ))
  idExReg.bits.dataSignals.pc := io.frontend.resp.bits.pc
  idExReg.bits.dataSignals.bpDestPc := branchPredictor.io.out.bits.pc
  idExReg.bits.dataSignals.bpTaken := branchPredictor.io.out.valid
  idExReg.bits.dataSignals.imm := MuxCase(0.U, Seq(
    (decoder.io.out.bits.value1 === Value1.U_IMM.asUInt) -> decodedInst.u_imm,
    (decoder.io.out.bits.value1 === Value1.UIMM19_15.asUInt) -> decodedInst.uimm19To15,
    (decoder.io.out.bits.value2 === Value2.I_IMM.asUInt) -> decodedInst.i_imm,
    (decoder.io.out.bits.value2 === Value2.S_IMM.asUInt) -> decodedInst.s_imm,
  ))

  val rs1ValueToEX = Mux(bypassingUnit.io.ID.out.rs1_value.valid, bypassingUnit.io.ID.out.rs1_value.bits, rf.io.rs1_out)
  val rs2ValueToEX = Mux(bypassingUnit.io.ID.out.rs2_value.valid, bypassingUnit.io.ID.out.rs2_value.bits, rf.io.rs2_out)

  idExReg.bits.dataSignals.rs1 := rs1ValueToEX
  idExReg.bits.dataSignals.rs2 := rs2ValueToEX
  idExReg.bits.dataSignals.zimm := decodedInst.zimm
  idExReg.bits.ctrlSignals.decode := decoder.io.out.bits
  idExReg.bits.ctrlSignals.rdIndex := decodedInst.rd

  // ベクトル命令を追加
  bypassingUnit.io.ID.in.rs1_index.bits := decodedInst.rs1
  // exceptionの際にこれを下げる必要があるかもしれない
  bypassingUnit.io.ID.in.rs1_index.valid := decoder.io.out.bits.useRs1 && decoder.io.out.valid && io.frontend.resp.valid
  bypassingUnit.io.ID.in.rs2_index.bits := decodedInst.rs2
  bypassingUnit.io.ID.in.rs2_index.valid := decoder.io.out.bits.useRs2 && decoder.io.out.valid && io.frontend.resp.valid

  rs1RequiredButNotValid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs1_bypassMatchAtEX -> (!bypassingUnit.io.EX.in.bits.rd.valid),
    bypassingUnit.io.ID.out.rs1_bypassMatchAtWB -> (!bypassingUnit.io.WB.in.bits.rd.valid),
  ))
  rs2RequiredButNotValid := MuxCase(false.B, Seq(
    bypassingUnit.io.ID.out.rs2_bypassMatchAtEX -> (!bypassingUnit.io.EX.in.bits.rd.valid),
    bypassingUnit.io.ID.out.rs2_bypassMatchAtWB -> (!bypassingUnit.io.WB.in.bits.rd.valid),
  ))

  val vecConfBypass = Wire(new VecCtrlUnitResp())
  val vtypeBypass = vecConfBypass.vtype

  vecConfBypass := Mux(vecCtrlUnit.io.resp.valid, vecCtrlUnit.io.resp.bits, exWbReg.bits.vectorCsrPorts.get)

  vectorDecoder.io.inst := decodedInst

  // ベクトル命令がベクトル設定・メモリアクセスでなく，かつvvならばvs1を使用する
  vrfReadyTable.io.vs1Check.valid := decoder.io.out.valid && decoder.io.out.bits.vector.get && !vectorDecoder.io.out.isConfsetInst && vectorDecoder.io.out.mop === MOP.NONE.asUInt && (vectorDecoder.io.out.vSource === VSOURCE.VV.asUInt)
  vrfReadyTable.io.vs1Check.bits.idx := decodedInst.rs1
  vrfReadyTable.io.vs1Check.bits.vtype := vtypeBypass
  vrfReadyTable.io.vs1Check.bits.vm := vectorDecoder.io.out.veuFun.isMaskInst
  // ベクトル設定命令でなく，かつメモリアクセスでないまたはインデックスならばvs2を使用する
  vrfReadyTable.io.vs2Check.valid := decoder.io.out.valid && decoder.io.out.bits.vector.get && !vectorDecoder.io.out.isConfsetInst && ((vectorDecoder.io.out.mop === MOP.NONE.asUInt) || (vectorDecoder.io.out.mop === MOP.IDX_ORDERED.asUInt))
  vrfReadyTable.io.vs2Check.bits.idx := decodedInst.rs2
  vrfReadyTable.io.vs2Check.bits.vtype := vtypeBypass
  vrfReadyTable.io.vs2Check.bits.vm := vectorDecoder.io.out.veuFun.isMaskInst
  // ベクトル設定命令でないならばvdを使用する
  vrfReadyTable.io.vdCheck.valid := decoder.io.out.valid && decoder.io.out.bits.vector.get && !vectorDecoder.io.out.isConfsetInst
  vrfReadyTable.io.vdCheck.bits.idx := decodedInst.rd
  vrfReadyTable.io.vdCheck.bits.vtype := vtypeBypass
  vrfReadyTable.io.vdCheck.bits.vm := vectorDecoder.io.out.veuFun.writeAsMask
  // vmフィールドが1ならばvmを使用する
  vrfReadyTable.io.vmCheck.valid := decoder.io.out.valid && decoder.io.out.bits.vector.get && !vectorDecoder.io.out.isConfsetInst && !vectorDecoder.io.out.vm
  // ベクトル設定命令でなく，かつストア命令で無い，ならばvdへ書き込む
  // vmv.{x.s, s.x}はチェイニングしないので無視する
  vrfReadyTable.io.invalidateVd := io.frontend.resp.valid && io.frontend.resp.ready && decoder.io.out.valid && decoder.io.out.bits.vector.get &&
    !vectorDecoder.io.out.isConfsetInst && !decoder.io.out.bits.memWrite && !vectorDecoder.io.out.vecPermutation

  // vecAluExecUnitを使用するなら，空いている方をvalidにする
  when(idFlush || idStall) {
    vecAluExecUnit.foreach(e => {
      e.io.signalIn.valid := false.B
      e.io.signalIn.bits := DontCare
    })
  } .elsewhen(io.frontend.resp.valid && decoder.io.out.valid && decoder.io.out.bits.vector.get && vectorDecoder.io.out.useVecAluExec) {
    val vecAluExecUnitAssigned = (0 until params.vecAluExecUnitNum).map(_ => WireInit(false.B))
    for ((x,i) <- vecAluExecUnit.zipWithIndex) {
      when(x.io.signalIn.ready && {
        val assignedSeq = vecAluExecUnitAssigned.slice(0, i)
        i match {
          case 0 => true.B
          case 1 => !assignedSeq(0)
          case _ => !assignedSeq.reduce(_ || _)
        }
      }) {
        printf("vecAluExecUnit%d valid\n", i.U)
        val vecSigs = x.io.signalIn.bits
        x.io.signalIn.valid := true.B
        vecSigs.vs1 := decodedInst.rs1
        vecSigs.vs2 := decodedInst.rs2
        vecSigs.vd := decodedInst.rd
        vecSigs.scalarVal := Mux(vectorDecoder.io.out.vSource === VSOURCE.VX.asUInt, rs1ValueToEX, decodedInst.imm19To15)
        vecSigs.vectorDecode := vectorDecoder.io.out
        vecSigs.scalarDecode := decoder.io.out.bits
        vecSigs.scalarDecode.vector.get := decoder.io.out.bits.vector.get
        vecSigs.vecConf := vecConfBypass
        vecSigs.pc := io.frontend.resp.bits.pc
        if(params.debug) {
          x.io.signalIn.bits.debug.get.instruction := decodedInst.bits
          x.io.signalIn.bits.debug.get.pc := io.frontend.resp.bits.pc
        }
        vecAluExecUnitAssigned(i) := true.B
      } .otherwise {
        x.io.signalIn := DontCare
        x.io.signalIn.valid := false.B
      }
    }
  } .otherwise {
    vecAluExecUnit.foreach(
      x => {
        x.io.signalIn := DontCare
        x.io.signalIn.valid := false.B
      }
    )
  }
  // vecLdstUnitに対しても同様
  when(idFlush || idStall) {
    vectorLdstUnit.io.signalIn.valid := false.B
    vectorLdstUnit.io.signalIn.bits := DontCare
  } .elsewhen(io.frontend.resp.valid && decoder.io.out.valid && decoder.io.out.bits.vector.get && vectorDecoder.io.out.useVecLdstExec && vecLdstUnitReady) {
    vectorLdstUnit.io.signalIn.valid := true.B
    vectorLdstUnit.io.signalIn.bits.scalar.rs2Value := rs2ValueToEX
    vectorLdstUnit.io.signalIn.bits.scalar.immediate := Mux(decoder.io.out.bits.memRead, decodedInst.i_imm, decodedInst.s_imm)
    vectorLdstUnit.io.signalIn.bits.scalar.rdIndex := decodedInst.rd
    val vecSigs = vectorLdstUnit.io.signalIn.bits.vector
    vecSigs.vs1 := decodedInst.rs1
    vecSigs.vs2 := decodedInst.rs2
    vecSigs.vd := decodedInst.rd
    vecSigs.scalarVal := rs1ValueToEX
    vecSigs.vectorDecode := vectorDecoder.io.out
    vecSigs.scalarDecode := decoder.io.out.bits
    vecSigs.vecConf := vecConfBypass
    vecSigs.pc := io.frontend.resp.bits.pc
    if(params.debug) {
      vecSigs.debug.get.pc := io.frontend.resp.bits.pc
      vecSigs.debug.get.instruction := decodedInst.bits
    }
  // スカラメモリアクセスの場合
  } .elsewhen(io.frontend.resp.valid && decoder.io.out.valid && decoder.io.out.bits.memValid && vecLdstUnitReady) {
    vectorLdstUnit.io.signalIn.valid := true.B
    vectorLdstUnit.io.signalIn.bits.scalar.rs2Value := rs2ValueToEX
    vectorLdstUnit.io.signalIn.bits.scalar.immediate := Mux(decoder.io.out.bits.memRead, decodedInst.i_imm, decodedInst.s_imm)
    vectorLdstUnit.io.signalIn.bits.scalar.rdIndex := decodedInst.rd
    vectorLdstUnit.io.signalIn.bits.vector := DontCare
    vectorLdstUnit.io.signalIn.bits.vector.scalarVal := rs1ValueToEX
    vectorLdstUnit.io.signalIn.bits.vector.scalarDecode := decoder.io.out.bits
    vectorLdstUnit.io.signalIn.bits.vector.pc := io.frontend.resp.bits.pc
    vectorLdstUnit.io.signalIn.bits.vector.vecConf := vecConfBypass
  } .otherwise {
    vectorLdstUnit.io.signalIn := DontCare
    vectorLdstUnit.io.signalIn.valid := false.B
  }

  when(decoder.io.out.valid && decoder.io.out.bits.vector.get) {
    idExReg.bits.vectorCtrlSignals.get := vectorDecoder.io.out
  }
  // 0 -> v0.mask[i]が1ならば書き込み，0ならば書き込まない
  // 1 -> マスクなし，全て書き込む
  // （マスクを使わないベクタ命令は全てvm=1か？）
  idExReg.bits.vectorDataSignals.get.mask := vectorDecoder.io.out.vm
  idExReg.bits.vectorDataSignals.get.vs1 := decodedInst.rs1
  idExReg.bits.vectorDataSignals.get.vs2 := decodedInst.rs2
  idExReg.bits.vectorDataSignals.get.vd := decodedInst.rd

  if (params.debug) {
    idExReg.bits.debug.get.instruction := decodedInst.bits
    idExReg.bits.debug.get.pc := io.frontend.resp.bits.pc
  }

  // retain the ID_EX register if stall
  when(exStall) {
    idExReg := idExReg
  }
  // flush the ID_EX register if branch miss, ecall, mret or exception
  idFlush := exFlush || branchEvaluator.io.out.valid
  when(idFlush) {
    idExReg.valid := false.B
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
    io.debugIo.get.vrfMap.get := vecRegFile.io.debug.get
  }

  alu.io.in1 := MuxLookup(idExReg.bits.ctrlSignals.decode.value1, 0.U)(Seq(
    Value1.RS1.asUInt -> idExReg.bits.dataSignals.rs1,
    Value1.U_IMM.asUInt -> idExReg.bits.dataSignals.imm,
  ))
  alu.io.in2 := MuxLookup(idExReg.bits.ctrlSignals.decode.value2, 0.U)(Seq(
    Value2.RS2.asUInt -> idExReg.bits.dataSignals.rs2,
    Value2.I_IMM.asUInt -> idExReg.bits.dataSignals.imm,
    Value2.S_IMM.asUInt -> idExReg.bits.dataSignals.imm,
    Value2.PC.asUInt -> idExReg.bits.dataSignals.pc.addr,
  ))
  alu.io.funct := idExReg.bits.ctrlSignals.decode

  branchEvaluator.io.req.bits.ALU_Result := alu.io.out
  branchEvaluator.io.req.bits.BranchType := idExReg.bits.ctrlSignals.decode.branch
  branchEvaluator.io.req.bits.destPC := idExReg.bits.dataSignals.bpDestPc
  branchEvaluator.io.req.bits.pc := idExReg.bits.dataSignals.pc
  branchEvaluator.io.req.bits.bp_taken := idExReg.bits.dataSignals.bpTaken
  branchEvaluator.io.req.valid := idExReg.valid

  if (params.useMulDiv) {
    val multiplier_hasValue = RegInit(false.B)
    // EXステージに有効な乗算命令があり，かつ乗算器の出力のvalidとreadyが共にtrueで無ければ乗算器に保持するべき情報（現在のEXステージの乗算命令）がある
    multiplier_hasValue := idExReg.bits.ctrlSignals.decode.useMul && idExReg.valid && !(multiplier.get.io.resp.ready && multiplier.get.io.resp.valid)
    multiplier.get.io.req.bits.rs1 := idExReg.bits.dataSignals.rs1
    multiplier.get.io.req.bits.rs2 := idExReg.bits.dataSignals.rs2
    multiplier.get.io.req.bits.funct := idExReg.bits.ctrlSignals.decode
    // 乗算器に保持するべき情報（現在のEXステージの乗算命令）があればvalidを下げる（乗算器が既に情報を受け取っているため）
    multiplier.get.io.req.valid := idExReg.bits.ctrlSignals.decode.useMul && !multiplier_hasValue && idExReg.valid && !exFlush
    multiplier.get.io.resp.ready := !(exWbReg.valid && wbStall)
  }

  // TODO: rs1がx0かつrdが非x0の場合にvlを最大に，rs1・rdともにx0の場合にvlを変更しないように仕様変更
  vecCtrlUnit.io.req.valid := idExReg.valid && idExReg.bits.ctrlSignals.decode.vector.get && idExReg.bits.vectorCtrlSignals.get.isConfsetInst
  vecCtrlUnit.io.req.bits.vDecode := idExReg.bits.vectorCtrlSignals.get
  vecCtrlUnit.io.req.bits.rs1_value := idExReg.bits.dataSignals.rs1
  vecCtrlUnit.io.req.bits.rs2_value := idExReg.bits.dataSignals.rs2
  vecCtrlUnit.io.req.bits.zimm := idExReg.bits.dataSignals.zimm
  vecCtrlUnit.io.req.bits.uimm := idExReg.bits.dataSignals.imm

  val exScalarRes = if (params.useMulDiv) {
    Mux(idExReg.bits.ctrlSignals.decode.useMul, multiplier.get.io.resp.bits, alu.io.out)
  } else {
    alu.io.out
  }

  // placefolder for vec->scalar inst (vcpop.m, vfirst.m, vmv.x.s)
  val exVectorRes = Mux(idExReg.bits.vectorCtrlSignals.get.vecPermutation, vecAluExecUnit(0).io.toExWbReg.bits.dataSignals.exResult, vecCtrlUnit.io.resp.bits.vl)

  bypassingUnit.io.EX.in.bits.rd.bits.index := idExReg.bits.ctrlSignals.rdIndex
  bypassingUnit.io.EX.in.bits.rd.bits.value := MuxLookup(idExReg.bits.ctrlSignals.decode.writeBackSelector, 0.U)(Seq(
    WB_SEL.PC4.asUInt -> idExReg.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH.asUInt -> exScalarRes,
    WB_SEL.VECTOR.asUInt -> exVectorRes,
  ))
  bypassingUnit.io.EX.in.bits.rd.valid := MuxLookup(idExReg.bits.ctrlSignals.decode.writeBackSelector, false.B)(Seq(
    WB_SEL.PC4.asUInt -> true.B,
    WB_SEL.ARITH.asUInt -> (if (params.useMulDiv) !idExReg.bits.ctrlSignals.decode.useMul || multiplier.get.io.resp.valid else true.B),
    WB_SEL.CSR.asUInt -> false.B,
    WB_SEL.MEM.asUInt -> false.B,
    WB_SEL.NONE.asUInt -> false.B,
    WB_SEL.VECTOR.asUInt -> (if (params.useVector) true.B else false.B)
  )) && idExReg.valid
  bypassingUnit.io.EX.in.valid := idExReg.bits.ctrlSignals.decode.writeToRd && idExReg.valid

  exWbReg.valid := idExReg.valid && (!idExReg.bits.ctrlSignals.decode.memValid || vectorLdstUnit.io.signalIn.ready) &&
    (if (params.useMulDiv) !idExReg.bits.ctrlSignals.decode.useMul || multiplier.get.io.resp.valid else true.B) &&
    (!idExReg.bits.ctrlSignals.decode.vector.get || idExReg.bits.vectorCtrlSignals.get.isConfsetInst || vecExecUnitsToExWbRegValid.reduce(_ || _))
  exWbReg.bits.dataSignals.pc := idExReg.bits.dataSignals.pc
  exWbReg.bits.dataSignals.exResult := MuxLookup(idExReg.bits.ctrlSignals.decode.writeBackSelector, 0.U)(Seq(
    WB_SEL.ARITH.asUInt -> exScalarRes,
    WB_SEL.VECTOR.asUInt -> exVectorRes,
  ))
  exWbReg.bits.dataSignals.datatoCSR := Mux(idExReg.bits.ctrlSignals.decode.value1 === Value1.RS1.asUInt, idExReg.bits.dataSignals.rs1, idExReg.bits.dataSignals.imm)
  exWbReg.bits.dataSignals.csrAddr := idExReg.bits.dataSignals.zimm

  exWbReg.bits.ctrlSignals := idExReg.bits.ctrlSignals

  exWbReg.bits.exceptionSignals.valid := (idExReg.valid && idExReg.bits.exceptionSignals.valid) || (idExReg.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt) && idExReg.valid
  // only machine-mode ecall and illegal inst is supported now
  exWbReg.bits.exceptionSignals.bits := MuxCase(0.U, Seq(
    // if there is already exception before ID, then retain
    idExReg.bits.exceptionSignals.valid -> idExReg.bits.exceptionSignals.bits,
    // else if exception in EX (load/store misaligned or access fault),
  ))
  Mux(idExReg.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt, 0xb.U(params.xprlen.W), 0.U)

  // EX_WB_REGに信号自体がvalidかを覚えさせておく
  when(vecCtrlUnit.io.resp.valid) {
    exWbReg.bits.vectorCsrPorts.get := vecCtrlUnit.io.resp.bits
  } .otherwise {
    exWbReg.bits.vectorCsrPorts.get := exWbReg.bits.vectorCsrPorts.get
  }

  if (params.debug) exWbReg.bits.debug.get := idExReg.bits.debug.get

  // WBステージがvalidかつ破棄できないかつEXステージに有効な値がある場合，またはメモリアクセス命令かつldstUnit.reqがreadyでない，または乗算命令で乗算器がvalidでない
  // またはベクタ命令実行完了前にスカラ命令がID_EXレジスタにある，またはチェイニング不可能なベクタ命令（構造ハザード・0要素目の値が用意できていないなど）
  exStall := idExReg.valid && ((exWbReg.valid && wbStall) || (if (params.useMulDiv) {
    idExReg.bits.ctrlSignals.decode.useMul && !multiplier.get.io.resp.valid
  } else false.B))
  // ベクトル命令がEXにある場合，IDがスカラ命令，またはIDのベクトル命令が発行できないならばIDの方でストールさせる

  // EX_WB_REGのvectorExecNumのデフォルト値
  exWbReg.bits.vectorExecNum.get.valid := false.B
  exWbReg.bits.vectorExecNum.get.bits := 0.U

  // リタイアするベクトル命令があればそれでEX_WB_REGを上書き
  for(d <- vecAluExecUnit) {
    when(d.io.toExWbReg.valid) {
      exWbReg := d.io.toExWbReg
      exWbReg.bits.vectorCsrPorts.get := exWbReg.bits.vectorCsrPorts.get
    }
  }
  when(vectorLdstUnit.io.toExWbReg.valid) {
    exWbReg := vectorLdstUnit.io.toExWbReg
    exWbReg.bits.vectorCsrPorts.get := exWbReg.bits.vectorCsrPorts.get
  }

  when(wbStall) {
    exWbReg := exWbReg
    exWbReg.bits.vectorCsrPorts.get := exWbReg.bits.vectorCsrPorts.get
  }

  // flush the EX_WB register if ecall, mret or exception
  exFlush := wbPcRedirect
  when(exFlush) {
    exWbReg.valid := false.B
  }

  // START OF WB STAGE
  // メモリアクセス命令かつ，ベクトル実行ユニットのベクトル命令がリタイアしない（toExWbRegがvalidでない）かつ，respがvalidでなければストール
  // 面倒くさいのでメモリ応答は常に1クロックで返ってくることにする
  wbStall := (if(true) false.B else exWbReg.valid && (exWbReg.bits.ctrlSignals.decode.memValid && !vectorLdstUnit.io.vectorResp.toVRF.valid && !vectorLdstUnit.io.scalarResp.valid))
  // let's just ignore exception
  val dmemoryAccessException = if(params.useException) {
    exWbReg.bits.ctrlSignals.decode.memValid && vectorLdstUnit.io.scalarResp.valid && vectorLdstUnit.io.scalarResp.bits.exceptionSignals.valid
  } else {
    false.B
  }
  wbPcRedirect := exWbReg.valid && (exWbReg.bits.ctrlSignals.decode.branch === Branch.MRET.asUInt
    || (if(params.useException) exWbReg.bits.exceptionSignals.valid || dmemoryAccessException else exWbReg.bits.ctrlSignals.decode.branch === Branch.ECALL.asUInt))

  when(wbPcRedirect) {
    io.frontend.req.bits.pc := csrUnit.io.resp.data
  }
  // 割り込みまたは例外の場合は、PCのみ更新しリタイアしない（命令を破棄）
  val wbInstCanRetire = exWbReg.valid && !(exWbReg.bits.exceptionSignals.valid || dmemoryAccessException) && !wbStall
  rf.io.req.valid := wbInstCanRetire && exWbReg.bits.ctrlSignals.decode.writeToRd
  rf.io.req.bits.data := MuxLookup(exWbReg.bits.ctrlSignals.decode.writeBackSelector, 0.U)(Seq(
    WB_SEL.PC4 -> exWbReg.bits.dataSignals.pc.nextPC,
    WB_SEL.ARITH -> exWbReg.bits.dataSignals.exResult,
    WB_SEL.CSR -> csrUnit.io.resp.data,
    WB_SEL.MEM -> vectorLdstUnit.io.scalarResp.bits.data,
    WB_SEL.VECTOR -> exWbReg.bits.dataSignals.exResult
  ).map {
    case (wb_sel, data) => (wb_sel.asUInt, data)
  })
  rf.io.req.bits.rd := exWbReg.bits.ctrlSignals.rdIndex

  bypassingUnit.io.WB.in.bits.rd.valid := bypassingUnit.io.WB.in.valid && (!exWbReg.bits.ctrlSignals.decode.memRead || vectorLdstUnit.io.scalarResp.valid)
  bypassingUnit.io.WB.in.bits.rd.bits.index := exWbReg.bits.ctrlSignals.rdIndex
  bypassingUnit.io.WB.in.bits.rd.bits.value := rf.io.req.bits.data
  bypassingUnit.io.WB.in.valid := exWbReg.bits.ctrlSignals.decode.writeToRd && wbInstCanRetire

  csrUnit.io.req.valid := exWbReg.valid
  // ecallやmretの処理はcsrUnit内で行われる
  csrUnit.io.req.bits.funct := exWbReg.bits.ctrlSignals.decode
  csrUnit.io.req.bits.data := exWbReg.bits.dataSignals.datatoCSR
  csrUnit.io.req.bits.csr_addr := exWbReg.bits.dataSignals.csrAddr
  csrUnit.io.fromCPU.hartid := io.hartid
  csrUnit.io.fromCPU.cpu_operating := cpuOperating
  csrUnit.io.fromCPU.inst_retire := wbInstCanRetire
  csrUnit.io.fromCPU.vectorExecNum.get := exWbReg.bits.vectorExecNum.get
  csrUnit.io.exception.valid := (exWbReg.bits.exceptionSignals.valid || dmemoryAccessException) && exWbReg.valid
  csrUnit.io.exception.bits.mepc_write := exWbReg.bits.dataSignals.pc.addr
  csrUnit.io.exception.bits.mcause_write := Mux(dmemoryAccessException, vectorLdstUnit.io.scalarResp.bits.exceptionSignals.bits, exWbReg.bits.exceptionSignals.bits)

  csrUnit.io.vectorCsrPorts.get := exWbReg.bits.vectorCsrPorts.get

  // EXまたはWBステージにfence, ecall, mretがある
  sysInstInPipeline := (idExReg.valid && idExReg.bits.ctrlSignals.decode.isSysInst) || (exWbReg.valid && exWbReg.bits.ctrlSignals.decode.isSysInst)

  if (params.debug) {
    io.debugIo.get.debugRetired.bits.instruction.bits := exWbReg.bits.debug.get.instruction & Fill(32, exWbReg.valid)
    io.debugIo.get.debugRetired.bits.pc.addr := exWbReg.bits.debug.get.pc.addr & Fill(params.xprlen, exWbReg.valid)
    io.debugIo.get.debugRetired.valid := wbInstCanRetire
    io.debugIo.get.debugAbiMap := rf.io.debug_abi_map.get
  }
}

object VectorCpu extends App {
  implicit val params: HajimeCoreParams = HajimeCoreParams(debug = false, useException = false)
  def apply(implicit params: HajimeCoreParams): VectorCpu = {
    if(params.useVector) new VectorCpu() else throw new Exception("fuck")
  }
  ChiselStage.emitSystemVerilogFile(new VectorCpu(), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}