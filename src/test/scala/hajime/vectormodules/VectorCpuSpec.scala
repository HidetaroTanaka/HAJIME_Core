package hajime.vectormodules

import chisel3._
import chiseltest._
import hajime.vectormodules.MemInitializer._
import hajime.simple4Stage._
import org.scalatest.flatspec._

class VectorCpuSpec extends AnyFlatSpec with ChiselScalatestTester {
  val instListWithoutDmem = Seq(
    "simple",
    "add", "addi", "addiw", "addw", "and", "andi", "auipc",
    "beq", "bge", "bgeu", "blt", "bltu", "bne", "jal", "jalr",
    "lui", "or", "ori", "sll", "slli", "slliw", "sllw",
    "slt", "slti", "sltiu", "sltu", "sra", "srai", "sraiw", "sraw",
    "srl", "srli", "srliw", "srlw", "sub", "subw", "xor", "xori"
  )
  for (e <- instListWithoutDmem) {
    it should s"Vector CPU pass the test ${e}" in {
      // 0x0000_0000 ~ 0x0000_1FFF : icache
      // 0x0000_4000 ~ 0x0000_5FFF : dcache
      // 0x1000_0000               : tohost
      test(new Core_and_cache(icache_memsize = 8192, dcache_memsize = 8192, tohost = 0x10000000, useVector = true, cpu = classOf[VectorCpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        dut.clock.setTimeout(1024)
        dut.io.reset_vector.poke(0.U)
        dut.io.hartid.poke(0.U)
        initialiseMemWithAxi(
          filename = s"src/main/resources/rv64ui/${e}_inst.hex",
          axi = dut.io.imem_initialiseAXI,
          initialising = dut.io.icache_initialising,
          clock = dut.clock,
          baseAddr = 0
        )
        while (dut.io.toHost.peek().litValue == 0) {
          dut.clock.step()
        }
        dut.io.toHost.bits.expect("h01".U(64.W))
        val toHost_Value = dut.io.toHost.bits.peek().litValue
        if (toHost_Value == 1) println(s"${e} test passed.") else println(s"${e} test failed at ${toHost_Value}")
        // println(s"IPC for ${e} test: ${c.io.performance_counters.retired_inst_count.peek().litValue.toDouble / c.io.performance_counters.cycle_count.peek().litValue.toDouble}")
        dut.clock.step()
      }
    }
  }
}
