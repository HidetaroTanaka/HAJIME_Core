package hajime.publicmodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common.{ScalarOpConstants, _}

class CSRUnitReq(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val funct = new ID_output
  val data = UInt(xprlen.W)
  val csr_addr = UInt(12.W)
}

class CSRUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Flipped(ValidIO(new CSRUnitReq()))
  val resp = Output(new CSRFileReadResp())
  val fromCPU = Input(new CPUtoCSR())
  val interrupt = Flipped(ValidIO(new CSRInterruptReq()))
}

class CSRUnit(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new CSRUnitIO())

  val csrFile = Module(CSRFile(params))

  csrFile.io.fromCPU := io.fromCPU
  // mretならばmepc，そうでなければ命令で指定されたcsr
  csrFile.io.csr_addr := Mux(io.req.bits.funct.branch === Branch.MRET.asUInt, CSRs.mepc.U(12.W), io.req.bits.csr_addr)
  csrFile.io.writeReq.bits.data := MuxLookup(io.req.bits.funct.csr_funct, io.req.bits.data)(Seq(
    CSR_FCN.C.asUInt -> (csrFile.io.readResp.data & ~io.req.bits.data),
    CSR_FCN.S.asUInt -> (csrFile.io.readResp.data | io.req.bits.data),
    CSR_FCN.W.asUInt -> io.req.bits.data,
  ))
  csrFile.io.writeReq.valid := MuxLookup(io.req.bits.funct.csr_funct, false.B)(Seq(
    CSR_FCN.N.asUInt -> false.B,
    CSR_FCN.C.asUInt -> true.B,
    CSR_FCN.S.asUInt -> true.B,
    CSR_FCN.W.asUInt -> true.B,
    CSR_FCN.R.asUInt -> false.B,
    CSR_FCN.I.asUInt -> false.B,
  ))

  io.resp := csrFile.io.readResp
  csrFile.io.interrupt := io.interrupt
}

object CSRUnit extends App {
  def apply(implicit params: HajimeCoreParams): CSRUnit = new CSRUnit()
  ChiselStage.emitSystemVerilogFile(CSRUnit(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}