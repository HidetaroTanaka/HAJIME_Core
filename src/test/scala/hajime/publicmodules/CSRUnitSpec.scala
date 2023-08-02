package hajime.publicmodules

import chisel3._
import chiseltest._
import hajime.common._
import org.scalatest.flatspec._

class CSRUnitSpec extends AnyFlatSpec with ChiselScalatestTester {
  it should "act correctly" in {
    val params = HajimeCoreParams()
    import params._
    test(CSRUnit(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.req.data.poke("h114514".U(xprlen.W))
      dut.io.req.csr_addr.poke("hF13".U(12.W)) // mimpid
      dut.io.req.CSR_func.poke(2.U) // CSR_SET
      dut.io.fromCPU.cpu_operating.poke(true.B)
      dut.io.fromCPU.inst_retire.poke(true.B)
      dut.io.fromCPU.hartid.poke(1.U)
      dut.io.resp.data.expect("h0001145141919810".U(xprlen.W))
      dut.clock.step()
      dut.io.req.data.poke("h114514".U(xprlen.W))
      dut.io.req.csr_addr.poke("hF13".U(12.W)) // mimpid
      dut.io.req.CSR_func.poke(2.U) // CSR_SET
      // mimpid is MRO, should not be overwritten
      dut.io.resp.data.expect("h0001145141919810".U(xprlen.W))
      dut.clock.step()
      // write to mtvec test
      dut.io.req.data.poke("h114514".U(xprlen.W))
      dut.io.req.csr_addr.poke("h305".U(12.W))
      dut.io.req.CSR_func.poke(3.U) // CSR_WRITE
      dut.clock.step()
      dut.io.resp.data.expect("h114514".U(xprlen.W))
      // clear mtvec test
      dut.io.req.data.poke("h00FF".U(xprlen.W))
      dut.io.req.CSR_func.poke(1.U) // CSR_CLEAR
      dut.clock.step()
      dut.io.resp.data.expect("h114500".U(xprlen.W))
      // set mtvec test
      dut.io.req.data.poke("hFF0000".U(xprlen.W))
      dut.io.req.CSR_func.poke(2.U) // CSR_SET
      dut.clock.step()
      dut.io.resp.data.expect("hFF4500".U(xprlen.W))
    }
  }
}
