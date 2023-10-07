package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO._
import hajime.common._
import hajime.publicmodules._
import chisel3.experimental.BundleLiterals._
import hajime.common.Functions.signExtend

class ScalarLdstSignalIn(implicit params: HajimeCoreParams) extends Bundle {
  // rs1はvectorReqのscalarValで良い
  // val rs1Value = UInt(params.xprlen.W)
  val rs2Value = UInt(params.xprlen.W)
  val immediate = UInt(params.xprlen.W)
  val scalarDecode = new ID_output()
}

class VectorLdstUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val signalIn = Flipped(DecoupledIO(new Bundle {
    val scalar = new ScalarLdstSignalIn()
    val vector = new VectorExecUnitSignalIn()
  }))
  val readVrf = Flipped(new VecRegFileReadIO())
  val scalarResp = ValidIO(new LDSTResp())
  val vectorResp = Output(new VectorExecUnitDataOut())
  val dcache = new AXI4liteIO(addr_width = params.xprlen, data_width = params.xprlen)
}

class VectorLdstUnit(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants with VectorOpConstants {
  require(params.useVector, "Let's all love Lain")
  val strb_width = params.xprlen/8

  val io = IO(new VectorLdstUnitIO())

  // scalar
  val scalarReqReg = RegInit(Valid(new ScalarLdstSignalIn()).Lit(
    _.valid -> false.B
  ))
  val scalarReqRegNext = RegNext(scalarReqReg)
  // vector
  val vectorReqReg = RegInit(Valid(new VectorExecUnitSignalIn()).Lit(
    _.valid -> false.B
  ))
  val vectorReqRegNext = RegNext(vectorReqReg)

  val vecIdx = RegInit(0.U(log2Up(params.vlen/8).W))
  val vecIdxToVrfWrite = RegNext(vecIdx)

  val accumulator = RegInit(0.U(params.xprlen.W))

  val vecMemAccessValid = vectorReqReg.valid
  // データメモリへのリクエストが命令の最終要素であれば次のIDステージの命令を得られる
  val vecMemAccessLast = vecMemAccessValid && (vecIdx === vectorReqReg.bits.vecConf.vl-1.U)
  //

  when(io.signalIn.valid && io.signalIn.ready) {
    scalarReqReg.valid := true.B
    scalarReqReg.bits := io.signalIn.bits.scalar
    vectorReqReg.valid := io.signalIn.bits.vector.vectorDecode.mop =/= MOP.NONE.asUInt
    vectorReqReg.bits := io.signalIn.bits.vector
  }.elsewhen(scalarReqReg.valid && (!vectorReqReg.valid || vecMemAccessLast)) {
    scalarReqReg.valid := false.B
    when(vectorReqReg.valid) {
      vectorReqReg.valid := false.B
    }
  }.otherwise {
    scalarReqReg := scalarReqReg
    vectorReqReg := vectorReqReg
  }
  assert(scalarReqReg.valid || !vectorReqReg.valid, "scalarReq false and vectorReq true")

  when((io.signalIn.valid && io.signalIn.ready) || vecMemAccessLast) {
    vecIdx := 0.U
    accumulator := 0.U
  } .elsewhen(vectorReqReg.valid) {
    vecIdx := vecIdx + 1.U
    // unit-stride
    // sew=0 -> accumulator += 1
    // sew=1 -> accumulator += 2
    // sew=2 -> accumulator += 4
    // sew=3 -> accumulator += 8
    // stride:
    // stride * (8 << sew)
    // sew=0 -> stride << 0
    // sew=1 -> stride << 1
    // sew=2 -> stride << 2
    // sew=3 -> stride << 3
    accumulator := accumulator + Mux(vectorReqReg.bits.vectorDecode.mop === MOP.UNIT_STRIDE.asUInt, MuxLookup(vectorReqReg.bits.vecConf.vtype.vsew, 1.U)(
      (0 until 4).map(i => i.U -> (1 << i).U)
    ), MuxLookup(vectorReqReg.bits.vecConf.vtype.vsew, scalarReqReg.bits.rs2Value)(
      (0 until 4).map(i => i.U -> (scalarReqReg.bits.rs2Value << i).asUInt)
    ))
  } .otherwise {
    vecIdx := 0.U
    accumulator := 0.U
  }

  // scalarReqRegがvalidでない（メモリアクセスが存在しない）または，axiのreqがreadyであり次のサイクルで命令を受け取れるかつベクトルならば最終要素
  io.signalIn.ready := !scalarReqReg.valid || (MuxCase(false.B, Seq(
    scalarReqReg.bits.scalarDecode.memRead -> io.dcache.ar.ready,
    scalarReqReg.bits.scalarDecode.memWrite -> (io.dcache.aw.ready && io.dcache.w.ready),
  )) && (!vectorReqReg.valid || vecMemAccessLast))

  val addr = vectorReqReg.bits.scalarVal + MuxLookup(vectorReqReg.bits.vectorDecode.mop, vectorReqReg.bits.scalarVal)(Seq(
    MOP.NONE -> scalarReqReg.bits.immediate,
    MOP.UNIT_STRIDE -> accumulator,
    MOP.IDX_UNORDERED -> io.readVrf.resp.vs2Out,
    MOP.STRIDED -> accumulator,
    MOP.IDX_ORDERED -> io.readVrf.resp.vs2Out,
  ).map(x => (x._1.asUInt, x._2)))
  val data = Mux(vectorReqReg.valid, io.readVrf.resp.vdOut, scalarReqReg.bits.rs2Value)

  // AXI4Lite Read Address Request Channel
  io.dcache.ar.valid := scalarReqReg.valid && scalarReqReg.bits.scalarDecode.memRead
  io.dcache.ar.bits.addr := addr
  io.dcache.ar.bits.prot := 0.U(3.W)

  // AXI4Lite Write Address Request Channel
  io.dcache.aw.valid := scalarReqReg.valid && scalarReqReg.bits.scalarDecode.memWrite && (!vectorReqReg.valid || vectorReqReg.bits.vectorDecode.vm || io.readVrf.resp.vm)
  io.dcache.aw.bits.addr := addr
  io.dcache.aw.bits.prot := 0.U(3.W)

  // AXI4Lite Write Response Channel
  io.dcache.b.ready := true.B

  // AXI4Lite Read Response Channel
  io.dcache.r.ready := true.B

  // AXI4Lite Write Data Request Channel
  io.dcache.w.valid := scalarReqReg.valid && scalarReqReg.bits.scalarDecode.memWrite && (!vectorReqReg.valid || vectorReqReg.bits.vectorDecode.vm || io.readVrf.resp.vm)
  io.dcache.w.bits.data := data
  io.dcache.w.bits.strb := MuxLookup(scalarReqReg.bits.scalarDecode.memory_length, 0.U(strb_width.W))(Seq(
    MEM_LEN.B.asUInt -> "h01".U(strb_width.W),
    MEM_LEN.H.asUInt -> "h03".U(strb_width.W),
    MEM_LEN.W.asUInt -> "h0F".U(strb_width.W),
    MEM_LEN.D.asUInt -> "hFF".U(strb_width.W)
  ))

  // toVRF
  io.readVrf.req.idx := vecIdx
  io.readVrf.req.sew := vectorReqReg.bits.vecConf.vtype.vsew
  io.readVrf.req.vs1 := 0.U
  io.readVrf.req.vs2 := vectorReqReg.bits.vs2
  io.readVrf.req.vd := vectorReqReg.bits.vd

  // scalar resp
  io.scalarResp.bits.data := io.dcache.r.bits.data
  io.scalarResp.bits.data := MuxLookup(scalarReqRegNext.bits.scalarDecode.memory_length, io.dcache.r.bits.data)(
    Seq(
      MEM_LEN.B -> 8,
      MEM_LEN.H -> 16,
      MEM_LEN.W -> 32,
      MEM_LEN.D -> 64,
    ).map {
      case (memLen: EnumType, width: Int) => memLen.asUInt -> Mux(scalarReqRegNext.bits.scalarDecode.mem_sext, io.dcache.r.bits.data(width - 1, 0).ext(params.xprlen), io.dcache.r.bits.data.zext.asUInt)
    }
  )
  io.scalarResp.valid := MuxCase(false.B, Seq(
    scalarReqRegNext.bits.scalarDecode.memRead -> io.dcache.ar.ready,
    scalarReqRegNext.bits.scalarDecode.memWrite -> (io.dcache.aw.ready && io.dcache.w.ready)
  )) && scalarReqRegNext.valid && (!vectorReqRegNext.valid || RegNext(vecMemAccessLast))
  io.scalarResp.bits.exceptionSignals.valid := false.B
  io.scalarResp.bits.exceptionSignals.bits := 0.U

  // vector resp
  io.vectorResp.toVRF.valid := scalarReqRegNext.valid && vectorReqRegNext.valid
  io.vectorResp.toVRF.bits.vd := vectorReqRegNext.bits.vd
  io.vectorResp.toVRF.bits.vtype := vectorReqRegNext.bits.vecConf.vtype
  io.vectorResp.toVRF.bits.index := vecIdxToVrfWrite
  io.vectorResp.toVRF.bits.last := RegNext(vecMemAccessLast)
  io.vectorResp.toVRF.bits.data := io.dcache.r.bits.data
  io.vectorResp.toVRF.bits.vm := false.B
  io.vectorResp.toVRF.bits.writeReq := io.vectorResp.toVRF.valid && RegNext(vectorReqReg.bits.vectorDecode.vm || io.readVrf.resp.vm) && scalarReqRegNext.bits.scalarDecode.memRead
}

object VectorLdstUnit extends App {
  implicit val params: HajimeCoreParams = HajimeCoreParams(useVector = true)
  def apply(implicit params: HajimeCoreParams): VectorLdstUnit = {
    require(params.useVector, "Vector Extension Required in VectorLdstUnit")
    new VectorLdstUnit()
  }
  ChiselStage.emitSystemVerilogFile(new VectorLdstUnit(), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
