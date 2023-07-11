import chisel3._
import chiseltest._
import org.scalatest.flatspec._

class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  it should "not act sussy" in {
    test(new Core_and_cache).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      for(_ <- 0 until 64) {
        c.clock.step()
      }
      c.io.debug.expect("h114514".U(64.W))
    }
  }
}
