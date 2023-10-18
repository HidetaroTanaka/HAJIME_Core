package hajime.simple4Stage

import chiseltest._
import Core_ApplicationTest._
import org.scalatest.flatspec._

class VectorTest extends AnyFlatSpec with ChiselScalatestTester {
  val vectorTestList = Seq(
    "vector_conf", "vector_ldst", "vector_memcpy"
  )
  for(e <- vectorTestList) {
    it should s"execute $e" in {
      test(new Core_and_cache(useVector = true, cpu = classOf[CPU])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        executeTest(dut, e, "vector")
      }
    }
  }
}
