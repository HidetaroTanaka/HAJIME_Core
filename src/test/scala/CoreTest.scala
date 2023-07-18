import chisel3._
import chiseltest._
import org.scalatest.flatspec._

class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  val instList_noDmem = Seq(
    "simple",
    "add", "addi", "addiw", "addw", "and", "andi", "auipc",
    "beq", "bge", "bgeu", "blt", "bltu", "bne", "jal", "jalr",
    "lui", "or", "ori", "sll", "slli", "slliw", "sllw",
    "slt", "slti", "sltiu", "sltu", "sra", "srai", "sraiw", "sraw",
    "srl", "srli", "srliw", "srlw", "sub", "subw", "xor", "xori"
  )
  for(e <- instList_noDmem) {
    it should s"pass the test ${e}" in {
      test(new Core_and_cache(icache_hexfilename = s"src/main/resources/${e}_inst.hex", dcache_hexfilename = null)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.clock.setTimeout(1024)
        while (c.io.debug.peek().litValue == 0) {
          c.clock.step()
        }
        c.io.debug.bits.expect("h01".U(64.W))
        val toHost_Value = c.io.debug.bits.peek().litValue
        if(toHost_Value == 1) println(s"${e} test passed.") else println(s"${e} test failed at ${toHost_Value}")
        println(s"IPC for ${e} test: ${c.io.performance_counters.retired_inst_count.peek().litValue.toDouble / c.io.performance_counters.cycle_count.peek().litValue.toDouble}")
      }
    }
  }
  val instList_withDmem = Seq(
    "lb", "lbu", "ld", "lh", "lhu", "lw", "lwu", // "ma_data",
    "sb", "sd", "sh", "sw"
  )
  for(e <- instList_withDmem) {
    it should s"pass the test ${e}" in {
      test(new Core_and_cache(icache_hexfilename = s"src/main/resources/${e}_inst.hex", dcache_hexfilename = s"src/main/resources/${e}_data.hex")).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.clock.setTimeout(1024)
        while (c.io.debug.peek().litValue == 0) {
          c.clock.step()
        }
        c.io.debug.bits.expect("h01".U(64.W))
        val toHost_Value = c.io.debug.bits.peek().litValue
        if (toHost_Value == 1) println(s"${e} test passed.") else println(s"${e} test failed at ${toHost_Value}")
        println(s"IPC for ${e} test: ${c.io.performance_counters.retired_inst_count.peek().litValue.toDouble / c.io.performance_counters.cycle_count.peek().litValue.toDouble}")
      }
    }
  }
}
