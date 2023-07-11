import chisel3._
import chiseltest._
import org.scalatest.flatspec._

class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  it should "not act sussy" in {
    test(new Core_and_cache).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      c.clock.setTimeout(4096)
      for(_ <- 0 until 512) {
        c.clock.step()
      }
      c.io.debug.expect("h01".U(64.W))
      c.io.debug_abi_map.gp.expect("h01".U(64.W))
    }
  }
}
