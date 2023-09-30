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

class ScalarLdstUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(DecoupledIO(new ScalarLdstSignalIn()))
  val resp = new ValidIO(new LDSTResp())
}

class VectorLdstUnit(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants with VectorOpConstants {
  require(params.useVector, "Let's all love Lain")

  val scalarIO = IO(new ScalarLdstUnitIO())
  val vectorIO = IO(new VectorExecUnitIO())
  val dcacheIO = IO(new AXI4liteIO(addr_width = params.xprlen, data_width = params.xprlen))

  // scalar
  val scalarReqReg = RegInit(Valid(new ScalarLdstSignalIn()).Lit(
    _.valid -> false.B
  ))
  // vector
  val vectorReqReg = RegInit(Valid(new VectorExecUnitSignalIn()).Lit(
    _.valid -> false.B
  ))

  when(scalarIO.req.valid && scalarIO.req.ready) {
    scalarReqReg.valid := true.B
    scalarReqReg.bits := scalarIO.req.bits
  }

  val vecIdx = RegInit(0.U(log2Up(params.vlen/8).W))
  val vecIdxToVrf = RegNext(vecIdx)

  val vecMemAccessValid = vectorReqReg.valid
  // データメモリへのリクエストが命令の最終要素であれば次のIDステージの命令を得られる
  val vecMemAccessLast = vecMemAccessValid && (vecIdx === vectorReqReg.bits.vecConf.vl-1.U)
  //
  when((vectorIO.signalIn.valid && vectorIO.signalIn.ready) || vecMemAccessLast) {
    vecIdx := 0.U
  }.otherwise {
    vecIdx := vecIdx + 1.U
  }

  val memReqReady = MuxCase(false.B, Seq(
    scalarReqReg.bits.scalarDecode.memRead -> dcacheIO.ar.ready,
    scalarReqReg.bits.scalarDecode.memWrite -> (dcacheIO.aw.ready && dcacheIO.w.ready),
  ))

  // scalarReqRegがvalidでないかつ，vectorReqRegがvalidでないまたは最終要素へのアクセス，
  // またはaxiのreqがreadyであり次のサイクルで命令を受け取れる
  scalarIO.req.ready := MuxCase(true.B, Seq(
    scalarReqReg.valid -> memReqReady,

  ))

  (!scalarReqReg.valid && (!vectorReqReg.valid || vecMemAccessLast) ||
    MuxCase(false.B, Seq(
    scalarReqReg.bits.scalarDecode.memRead -> dcacheIO.ar.ready,
    scalarReqReg.bits.scalarDecode.memWrite -> (dcacheIO.aw.ready && dcacheIO.w.ready),
  )))


}
