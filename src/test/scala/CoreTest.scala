import chisel3._
import chiseltest._
import org.scalatest.flatspec._

class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  val instList_noDmem = Seq(
    "add", "addi", "addiw", "addw", "and", "andi", "auipc",
    "beq", "bge", "bgeu", "blt", "bltu", "bne", "jal", "jalr"
  )
  for(e <- instList_noDmem) {
    it should s"pass the test ${e}" in {
      test(new Core_and_cache(testname = e, initialiseDmem = false)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.clock.setTimeout(1024)
        while (c.io.debug.peek().litValue == 0) {
          c.clock.step()
        }
        // c.io.debug.expect("h01".U(64.W))
        c.io.debug_abi_map.gp.expect("h01".U(64.W))
        println(s"${e} inst test passed.")
      }
    }
  }
  val instList_withDmem = Seq(
    "lb"
  )
  for(e <- instList_withDmem) {
    it should s"pass the test ${e}" in {
      test(new Core_and_cache(testname = e, initialiseDmem = true)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.clock.setTimeout(1024)
        while (c.io.debug.peek().litValue == 0) {
          c.clock.step()
        }
        // c.io.debug.expect("h01".U(64.W))
        c.io.debug_abi_map.gp.expect("h01".U(64.W))
        println(s"${e} inst test passed.")
      }
    }
  }
}
