package hajime.publicmodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common._

class CSRUnitReq(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val funct = new ID_output
  val data = UInt(xprlen.W)
  val csr = UInt(12.W)
}

class CSRUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Input(new CSRUnitReq())
  val resp = Output(new CSRFileReadResp())
  val fromCPU = Input(new CPUtoCSR())
}

class CSRUnit(implicit params: HajimeCoreParams) extends Module with ScalarOpConstants {
  val io = IO(new CSRUnitIO())

  val csrFile = Module(CSRFile(params))

  csrFile.io.fromCPU := io.fromCPU
  csrFile.io.readReq.csr := io.req.csr
  csrFile.io.writeReq.bits.csr := io.req.csr
  csrFile.io.writeReq.bits.data := MuxLookup(io.req.funct.csr_funct, io.req.data)(Seq(
    CSR_FCN.C.asUInt -> (csrFile.io.readResp.data & ~io.req.data),
    CSR_FCN.S.asUInt -> (csrFile.io.readResp.data | io.req.data),
    CSR_FCN.W.asUInt -> io.req.data
  ))
  csrFile.io.writeReq.valid := true.B

  io.resp := csrFile.io.readResp
}

object CSRUnit extends App {
  def apply(implicit params: HajimeCoreParams): CSRUnit = new CSRUnit()
  ChiselStage.emitSystemVerilogFile(CSRUnit(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}