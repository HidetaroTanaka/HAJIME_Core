import chisel3._
import chiseltest._
import org.scalatest.flatspec._

class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  val instList = Seq("add", "addi")
  for(e <- instList) {
    it should s"pass the test ${e}" in {
      test(new Core_and_cache(testname = e, initialiseDmem = false)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
        c.clock.setTimeout(1024)
        while (c.io.debug.peek().litValue != 1) {
          c.clock.step()
        }
        // c.io.debug.expect("h01".U(64.W))
        c.io.debug_abi_map.gp.expect("h01".U(64.W))
        println(s"${e} inst test passed.")
      }
    }
  }
}
