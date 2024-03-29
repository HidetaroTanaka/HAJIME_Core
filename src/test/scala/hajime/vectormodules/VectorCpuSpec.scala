package hajime.vectormodules

import chisel3._
import chiseltest._
import hajime.simple4Stage.Core_ApplicationTest._
import hajime.vectormodules.MemInitializer._
import hajime.simple4Stage._
import org.scalatest.flatspec._

class VectorCpuSpec extends AnyFlatSpec with ChiselScalatestTester {
  val instListWithoutDmem: Seq[String] = if(true) Seq(
    "simple",
    "add", "addi", "addiw", "addw", "and", "andi", "auipc",
    "beq", "bge", "bgeu", "blt", "bltu", "bne", "jal", "jalr",
    "lui", "or", "ori", "sll", "slli", "slliw", "sllw",
    "slt", "slti", "sltiu", "sltu", "sra", "srai", "sraiw", "sraw",
    "srl", "srli", "srliw", "srlw", "sub", "subw", "xor", "xori"
  ) else Nil
  for (e <- instListWithoutDmem) {
    it should s"Vector CPU pass the test ${e}" in {
      // 0x0000_0000 ~ 0x0000_1FFF : icache
      // 0x0000_4000 ~ 0x0000_5FFF : dcache
      // 0x1000_0000               : tohost
      test(new CoreAndCache(iCacheMemsize = 8192, dCacheMemsize = 8192, tohost = 0x10000000, useVector = true, cpu = classOf[VectorCpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        dut.clock.setTimeout(1024)
        dut.io.resetVector.poke(0.U)
        dut.io.hartid.poke(0.U)
        initialiseMemWithAxi(
          filename = s"src/main/resources/rv64ui/${e}_inst.hex",
          axi = dut.io.iMemInitialiseAxi,
          initialising = dut.io.iCacheInitialising,
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

  val instListWithDmem = if(true) Seq(
    "lb", "lbu", "ld", "lh", "lhu", "lw", "lwu",
    "sb", "sd", "sh", "sw"
  ) else Nil
  for (e <- instListWithDmem) {
    it should s"Vector CPU pass the test ${e}" in {
      test(new CoreAndCache(iCacheMemsize = 8192, dCacheMemsize = 8192, tohost = 0x10000000, useVector = true, cpu = classOf[VectorCpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        dut.clock.setTimeout(1024)
        dut.io.resetVector.poke(0.U)
        dut.io.hartid.poke(0.U)
        fork {
          initialiseMemWithAxi(
            filename = s"src/main/resources/rv64ui/${e}_inst.hex",
            axi = dut.io.iMemInitialiseAxi,
            initialising = dut.io.iCacheInitialising,
            clock = dut.clock,
            baseAddr = 0
          )
        }.fork {
          initialiseMemWithAxi(
            filename = s"src/main/resources/rv64ui/${e}_data.hex",
            axi = dut.io.dMemInitialiseAxi,
            initialising = dut.io.dCacheInitialising,
            clock = dut.clock,
            baseAddr = 0x4000
          )
        }.join()
        while (dut.io.toHost.peek().litValue == 0) {
          dut.clock.step()
        }
        // dut.io.toHost.bits.expect("h01".U(64.W))
        val toHost_Value = dut.io.toHost.bits.peek().litValue
        if (toHost_Value == 1) println(s"${e} test passed.") else println(s"${e} test failed at ${toHost_Value}")
        // println(s"IPC for ${e} test: ${c.io.performance_counters.retired_inst_count.peek().litValue.toDouble / c.io.performance_counters.cycle_count.peek().litValue.toDouble}")
      }
    }
  }
  val instListMult = if(true) Seq(
    "mul", "mulh", "mulhsu", "mulhu", "mulw"
  ) else Nil
  for (e <- instListMult) {
    it should s"Vector CPU pass the test ${e}" in {
      test(new CoreAndCache(iCacheMemsize = 8192, dCacheMemsize = 8192, tohost = 0x10000000, useVector = true, cpu = classOf[VectorCpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        dut.clock.setTimeout(1024)
        dut.io.resetVector.poke(0.U)
        dut.io.hartid.poke(0.U)
        initialiseMemWithAxi(
          filename = s"src/main/resources/rv64um/${e}_inst.hex",
          axi = dut.io.iMemInitialiseAxi,
          initialising = dut.io.iCacheInitialising,
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
      }
    }
  }
}

class Rv64imAppTestForVecCpu extends AnyFlatSpec with ChiselScalatestTester {
  val rv64iTestList = Seq(
    "helloworld", "median", "printInt64", "selection_sort", "memcpy", "quicksort"
  )
  for (e <- rv64iTestList) {
    it should s"Vector CPU execute $e" in {
      test(new CoreAndCache(useVector = true, cpu = classOf[VectorCpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        executeTest(dut, e, "rv64i")
      }
    }
  }
  val rv64mTestList = Seq(
    "factorial", "power", "vector_innerproduct"
  )
  for (e <- rv64mTestList) {
    it should s"Vector CPU execute $e" in {
      test(new CoreAndCache(useVector = true, cpu = classOf[VectorCpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        executeTest(dut, e, "rv64m")
      }
    }
  }
}

class Zve64xAppTestForVecCpu extends AnyFlatSpec with ChiselScalatestTester {
  val ldstTest = Seq("vector_conf", "vector_ldst", "vector_memcpy", "vector_stride", "vector_index")
  val arithmeticTest = Seq(
    "vadd", "vsub", "vand", "vmseq", "vmslt", "vmsle", "vmsgt",
    "vminmax", "vmerge", "vmv",
    "vmand", "vmor", "vmxor",
    "vmul", "vmulh", "vmulhu", "vmulhsu",
    "vmacc", "vnmsac", "vmadd", "vnmsub",
    "vredsum",
  )
  val applicationTest = Seq(
    "vector_median", "vector_matmul"
  )
  val zve64xTestList: Seq[String] = ldstTest ++ arithmeticTest ++ applicationTest
  for (e <- zve64xTestList) {
    it should s"Vector CPU execute $e" in {
      test(new CoreAndCache(useVector = true, cpu = classOf[VectorCpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        executeTest(dut, e, "vector")
      }
    }
  }
}

class FpgaTestForVecCpu extends AnyFlatSpec with ChiselScalatestTester {
  def _executeTest[T <: CpuModule](dut: CoreAndCache[T], testName: String, testType: String): Unit = {
    println(s"test $testName:")
    fork {
      initialiseMemWithAxi(s"src/main/resources/applications_${testType}/${testName}_inst.mem", dut.io.iMemInitialiseAxi, dut.io.iCacheInitialising, dut.clock, 0)
    }.fork {
      initialiseMemWithAxi(s"src/main/resources/applications_${testType}/${testName}_data.mem", dut.io.dMemInitialiseAxi, dut.io.dCacheInitialising, dut.clock, 0x4000)
    }.join()
    dut.clock.setTimeout(0)
    dut.io.resetVector.poke(0.U)
    dut.io.hartid.poke(0.U)

    for(_ <- 0 until 1048576) {
      dut.clock.step()
    }
    println(dut.io.toHost.bits.peekInt())
  }
  it should "Vector CPU execute matmul for FPGA" in {
    test(new CoreAndCache(useVector = true, cpu = classOf[VectorCpu])).withAnnotations(Seq(WriteVcdAnnotation, IcarusBackendAnnotation)) { dut =>
      _executeTest(dut, "vector_matmul", "fpga")
    }
  }
}