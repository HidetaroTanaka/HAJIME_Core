package hajime.simple4Stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec._
import hajime.vectormodules.MemInitializer._

object Core_ApplicationTest {
  def get_toHostChar(dut: Core_and_cache): Char = {
    dut.io.toHost.bits.peekInt().toChar
  }

  def get_toHostValid(dut: Core_and_cache): Boolean = {
    dut.io.toHost.valid.peekBoolean()
  }

  def executeTest(dut: Core_and_cache, testName: String, testType: String): Unit = {
    println(s"test $testName:")
    fork {
      initialiseMemWithAxi(s"src/main/resources/applications_${testType}/${testName}_inst.hex", dut.io.imem_initialiseAXI, dut.io.icache_initialising, dut.clock, 0)
    }.fork {
      initialiseMemWithAxi(s"src/main/resources/applications_${testType}/${testName}_data.hex", dut.io.dmem_initialiseAXI, dut.io.dcache_initialising, dut.clock, 0x4000)
    }.join()
    dut.clock.setTimeout(65536)
    dut.io.reset_vector.poke(0.U)
    dut.io.hartid.poke(0.U)

    var toHostWrittenChar: List[Char] = Nil
    while (!(get_toHostValid(dut) && (get_toHostChar(dut) == '\u0000'))) {
      dut.clock.step()
      if (get_toHostValid(dut)) {
        // print(get_toHostChar())
        toHostWrittenChar = toHostWrittenChar :+ get_toHostChar(dut)
      }
    }
    dut.io.debug_io.debug_abi_map.a0.expect(0.U(64.W))
    toHostWrittenChar.foreach(print)
    println()
  }
}

import Core_ApplicationTest._

class Rv64iApplicationTest extends AnyFlatSpec with ChiselScalatestTester {
  val rv64iTestList = Seq(
    "helloworld", "median", "printInt64", "selection_sort", "memcpy", "quicksort"
  )
  for(e <- rv64iTestList) {
    it should s"execute $e" in {
      test(new Core_and_cache()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        executeTest(dut, e, "rv64i")
      }
    }
  }
}

class Rv64mApplicationTest extends AnyFlatSpec with ChiselScalatestTester {
  val rv64mTestList = Seq(
    "factorial", "power", "vector_innerproduct"
  )
  for(e <- rv64mTestList) {
    it should s"execute $e" in {
      test(new Core_and_cache()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        executeTest(dut, e, "rv64m")
      }
    }
  }
}

class ExceptionApplicationTest extends AnyFlatSpec with ChiselScalatestTester {
  val exceptionTestList = Seq(
    "illegal_inst", "ecall", "inst_access_fault", "inst_access_misaligned",
    "load_address_misaligned", "load_access_fault", "store_address_misaligned", "store_access_fault",
  )
  for(e <- exceptionTestList) {
    it should s"execute $e" in {
      test(new Core_and_cache()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        executeTest(dut, e, "exceptions")
      }
    }
  }
}
