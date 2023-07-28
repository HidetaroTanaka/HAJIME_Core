package hajime.publicmodules

import chisel3._
import chisel3.util._
import hajime.common._
import hajime.common.Deprecated_ScalarOpConstants._

class CSRUnitReq(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val csr = UInt(12.W)
  val data = UInt(xprlen.W)
  val CSR_func = UInt(CSR_NONE.getWidth.W)
}

class CSRUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Input(new CSRUnitReq())
  val resp = Output(new CSRFileReadResp())
  val fromCPU = Input(new CPUtoCSR())
}

class CSRUnit(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new CSRUnitIO())

  val csrFile = Module(CSRFile(params))

  csrFile.io.fromCPU := io.fromCPU
  csrFile.io.readReq.csr := io.req.csr
  csrFile.io.writeReq.bits.csr := io.req.csr
  csrFile.io.writeReq.bits.data := MuxLookup(io.req.CSR_func, io.req.data)(Seq(
    CSR_CLEAR -> (csrFile.io.readResp.data & ~io.req.data),
    CSR_SET -> (csrFile.io.readResp.data | io.req.data),
    CSR_WRITE -> io.req.data
  ))
  csrFile.io.writeReq.valid := true.B

  io.resp := csrFile.io.readResp
}

object CSRUnit extends App {
  def apply(implicit params: HajimeCoreParams): CSRUnit = new CSRUnit()
  (new chisel3.stage.ChiselStage).emitVerilog(apply(HajimeCoreParams()), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}