package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.axiIO._
import hajime.common.{HajimeCoreParams, ScalarOpConstants}
import hajime.publicmodules._
import chisel3.experimental.BundleLiterals._

class ScalarLdstSignalIn(implicit params: HajimeCoreParams) extends Bundle {
  val rs1Value = UInt(params.xprlen.W)
  val rs2Value = UInt(params.xprlen.W)
  val immediate = UInt(params.xprlen.W)
  val scalarDecode = new ID_output()
}

class VectorLdstUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val signalIn = Flipped(DecoupledIO(new Bundle {
    val scalar = new ScalarLdstSignalIn()
    val vector = new VectorExecUnitSignalIn()
  }))
  val scalarResp = ValidIO(new LDSTResp())
  val vectorResp = Output(new VectorExecUnitDataOut())
  val dcache = new AXI4liteIO(addr_width = params.xprlen, data_width = params.xprlen)
}

class VectorLdstUnit(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants with VectorOpConstants {
  require(params.useVector, "Let's all love Lain")

  val io = IO(new VectorLdstUnitIO())

  // scalar
  val scalarReqReg = RegInit(Valid(new ScalarLdstSignalIn()).Lit(
    _.valid -> false.B
  ))
  // vector
  val vectorReqReg = RegInit(Valid(new VectorExecUnitSignalIn()).Lit(
    _.valid -> false.B
  ))

  when(io.signalIn.valid && io.signalIn.ready) {
    scalarReqReg.valid := true.B
    scalarReqReg.bits := io.signalIn.bits.scalar
    vectorReqReg.valid := io.signalIn.bits.vector.vectorDecode.mop =/= MOP.NONE.asUInt
    vectorReqReg.bits := io.signalIn.bits.vector
  }
  assert(scalarReqReg.valid || !vectorReqReg.valid, "scalarReq false and vectorReq true")

  val vecIdx = RegInit(0.U(log2Up(params.vlen/8).W))
  val vecIdxToVrf = RegNext(vecIdx)

  val vecMemAccessValid = vectorReqReg.valid
  // データメモリへのリクエストが命令の最終要素であれば次のIDステージの命令を得られる
  val vecMemAccessLast = vecMemAccessValid && (vecIdx === vectorReqReg.bits.vecConf.vl-1.U)
  //
  when((io.signalIn.valid && io.signalIn.ready) || vecMemAccessLast) {
    vecIdx := 0.U
  }.otherwise {
    vecIdx := vecIdx + 1.U
  }

  val memReqReady = MuxCase(false.B, Seq(
    scalarReqReg.bits.scalarDecode.memRead -> io.dcache.ar.ready,
    scalarReqReg.bits.scalarDecode.memWrite -> (io.dcache.aw.ready && io.dcache.w.ready),
  ))

  // scalarReqRegがvalidでない（メモリアクセスが存在しない）または，axiのreqがreadyであり次のサイクルで命令を受け取れるかつベクトルならば最終要素
  io.signalIn.ready := !scalarReqReg.valid || (MuxCase(false.B, Seq(
    scalarReqReg.bits.scalarDecode.memRead -> io.dcache.ar.ready,
    scalarReqReg.bits.scalarDecode.memWrite -> (io.dcache.aw.ready && io.dcache.w.ready),
  )) && (!vectorReqReg.valid || vecMemAccessLast))

}
