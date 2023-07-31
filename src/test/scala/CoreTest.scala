import chisel3._
import chiseltest._
import org.scalatest.flatspec._
import scala.sys.process._

/*
class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  val instList_noDmem = Seq(
    "simple",
    "add", "addi", "addiw", "addw", "and", "andi", "auipc",
    "beq", "bge", "bgeu", "blt", "bltu", "bne", "jal", "jalr",
    "lui", "or", "ori", "sll", "slli", "slliw", "sllw",
    "slt", "slti", "sltiu", "sltu", "sra", "srai", "sraiw", "sraw",
    "srl", "srli", "srliw", "srlw", "sub", "subw", "xor", "xori"
  )
  // make it true only when recompile riscv-tests
  if(false) {
    instList_noDmem.foreach(e => {
      Process(Seq("sh", "-c", s"export PATH=\"$$PATH:/opt/riscv/riscv-gnu-toolchain/bin\" && cd ~/github/HAJIME_Core/src/main/resources && sh build.sh ${e}")).!
    })
  }
  for(e <- instList_noDmem) {
    it should s"pass the test ${e}" in {
      // 0x0000_0000 ~ 0x0000_1FFF : icache
      // 0x0000_4000 ~ 0x0000_5FFF : dcache
      // 0x1000_0000               : tohost
      test(new Core_and_cache(icache_hexfilename = s"src/main/resources/rv64ui/${e}_inst.hex", dcache_hexfilename = null, icache_memsize = 8192, dcache_memsize = 8192, tohost = 0x10000000)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.clock.setTimeout(1024)
        while (c.io.toHost.peek().litValue == 0) {
          c.clock.step()
        }
        c.io.toHost.bits.expect("h01".U(64.W))
        val toHost_Value = c.io.toHost.bits.peek().litValue
        if(toHost_Value == 1) println(s"${e} test passed.") else println(s"${e} test failed at ${toHost_Value}")
        println(s"IPC for ${e} test: ${c.io.performance_counters.retired_inst_count.peek().litValue.toDouble / c.io.performance_counters.cycle_count.peek().litValue.toDouble}")
      }
    }
  }
  val instList_withDmem = Seq(
    "lb", "lbu", "ld", "lh", "lhu", "lw", "lwu", "ma_data",
    "sb", "sd", "sh", "sw"
  )
  // make it true only when recompile riscv-tests
  if (false) {
    instList_withDmem.foreach(e => {
      Process(Seq("sh", "-c", s"export PATH=\"$$PATH:/opt/riscv/riscv-gnu-toolchain/bin\" && cd ~/github/HAJIME_Core/src/main/resources && sh build.sh ${e}")).!
    })
  }
  for(e <- instList_withDmem) {
    it should s"pass the test ${e}" in {
      test(new Core_and_cache(icache_hexfilename = s"src/main/resources/rv64ui/${e}_inst.hex", dcache_hexfilename = s"src/main/resources/rv64ui/${e}_data.hex", icache_memsize = 8192, dcache_memsize = 8192, tohost = 0x10000000)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.clock.setTimeout(if(e == "ma_data") 4096 else 1024)
        while (c.io.toHost.peek().litValue == 0) {
          c.clock.step()
        }
        c.io.toHost.bits.expect("h01".U(64.W))
        val toHost_Value = c.io.toHost.bits.peek().litValue
        if (toHost_Value == 1) println(s"${e} test passed.") else println(s"${e} test failed at ${toHost_Value}")
        println(s"IPC for ${e} test: ${c.io.performance_counters.retired_inst_count.peek().litValue.toDouble / c.io.performance_counters.cycle_count.peek().litValue.toDouble}")
      }
    }
  }
}
*/
