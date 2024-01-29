package hajime.publicmodules

import chisel3._
import chiseltest._
import hajime.common._
import org.scalatest.flatspec._
import chisel3.experimental.BundleLiterals._

class CSRUnitSpec extends AnyFlatSpec with ChiselScalatestTester with ScalarOpConstants {
  it should "act correctly" in {
    implicit val params = HajimeCoreParams(useVector = false)
    import params._
    test(CSRUnit(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // should read mimpid
      dut.io.req.bits.data.poke("h114514".U(xprlen.W))
      dut.io.req.bits.csr_addr.poke(CSRs.mimpid.U(12.W))
      dut.io.req.bits.funct.poke((new ID_output()).Lit(
        _.branch -> 0.U,
        _.value1 -> Value1.RS1.litValue.U,
        _.value2 -> Value2.ZERO.litValue.U,
        _.arithmeticFunct -> ARITHMETIC_FCN.NONE.litValue.U,
        _.aluFlag -> false.B,
        _.op32 -> false.B,
        _.writeBackSelector -> WB_SEL.CSR.litValue.U,
        _.memoryFunction -> MEM_FCN.M_NONE.litValue.U,
        _.memoryLength -> MEM_LEN.B.litValue.U,
        _.memSExt -> false.B,
        _.csrFunct -> CSR_FCN.S.litValue.U,
        _.fence -> false.B,
      ))
      dut.io.req.valid.poke(true.B)
      dut.io.fromCPU.cpu_operating.poke(true.B)
      dut.io.fromCPU.inst_retire.poke(true.B)
      dut.io.fromCPU.hartid.poke(1.U)
      dut.io.exception.valid.poke(false.B)
      dut.io.exception.bits.mcause_write.poke(0.U)
      dut.io.exception.bits.mepc_write.poke(0.U)
      dut.io.resp.data.expect("h0001145141919810".U(xprlen.W))
      dut.clock.step()
      // mimpid is MRO, should not be overwritten
      dut.io.resp.data.expect("h0001145141919810".U(xprlen.W))
      dut.clock.step()
      // write to mtvec test
      dut.io.req.bits.data.poke("h1919".U(xprlen.W))
      dut.io.req.bits.csr_addr.poke(CSRs.mtvec.U(12.W))
      dut.io.req.bits.funct.csrFunct.poke(CSR_FCN.W.litValue.U) // CSR_WRITE
      dut.clock.step()
      dut.io.resp.data.expect("h1919".U(xprlen.W))
      dut.clock.step()
      // ecall test
      dut.io.exception.valid.poke(true.B)
      dut.io.exception.bits.mcause_write.poke(Causes.machine_ecall)
      dut.io.exception.bits.mepc_write.poke("h00008040".U(xprlen.W))
      // mtvec(63,2), 2'b00
      dut.io.resp.data.expect("h1918".U)
      dut.clock.step()
      // mret test
      dut.io.exception.valid.poke(false.B)
      dut.io.req.bits.funct.branch.poke(Branch.MRET.litValue.U)
      dut.io.resp.data.expect("h00008040".U(xprlen.W))
      dut.clock.step()
    }
  }
}
