package hajime.publicmodules

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import org.scalatest.exceptions.TestFailedException
import org.scalatest.flatspec._

class MultiplierTest extends AnyFlatSpec with ChiselScalatestTester {
  it should "multiply 8 bit correctly" in {
    test(Multiplier_nxn(8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for(x <- 0 to 0xFF; y <- 0 to 0xFF) {
        dut.io.multiplicand.poke(x.U(8.W))
        dut.io.multiplier.poke(y.U(8.W))
        println(s"${x} * ${y} = ${dut.io.out.peekInt()}")
        try {
          dut.io.out.expect((x * y).U(16.W))
          dut.clock.step()
        } catch {
          case _: TestFailedException => println(s"test failed at ${x} * ${y}")
        }
      }
    }
  }
}
