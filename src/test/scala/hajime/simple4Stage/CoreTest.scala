package hajime.simple4Stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec._

import scala.io._
import scala.sys.process._
import scala.collection.parallel.CollectionConverters._

class CoreTest extends AnyFlatSpec with ChiselScalatestTester {
  def initialiseImem[T <: CpuModule](filename: String, dut: CoreAndCache[T]): Unit = {
    hajime.vectormodules.MemInitializer.initialiseMemWithAxi(filename, dut.io.iMemInitialiseAxi, dut.io.iCacheInitialising, dut.clock, 0)
  }

  def initialiseDmem[T <: CpuModule](filename: String, dut: CoreAndCache[T]): Unit = {
    hajime.vectormodules.MemInitializer.initialiseMemWithAxi(filename, dut.io.dmem_initialiseAXI, dut.io.dCacheInitialising, dut.clock, 0x4000)
  }

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
    Process(Seq("bash", "-c", s"cd ~/github/HAJIME_Core/src/main/resources/rv64ui && rm *_data.hex")).!
  }
  for(e <- instList_noDmem) {
    it should s"pass the test ${e}" in {
      // 0x0000_0000 ~ 0x0000_1FFF : icache
      // 0x0000_4000 ~ 0x0000_5FFF : dcache
      // 0x1000_0000               : tohost
      test(new CoreAndCache(iCacheMemsize = 8192, dCacheMemsize = 8192, tohost = 0x10000000, cpu = classOf[Cpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        dut.clock.setTimeout(1024)
        dut.io.resetVector.poke(0.U)
        dut.io.hartid.poke(0.U)
        initialiseImem(s"src/main/resources/rv64ui/${e}_inst.hex", dut)
        while (dut.io.toHost.peek().litValue == 0) {
          dut.clock.step()
        }
        dut.io.toHost.bits.expect("h01".U(64.W))
        val toHost_Value = dut.io.toHost.bits.peek().litValue
        if(toHost_Value == 1) println(s"${e} test passed.") else println(s"${e} test failed at ${toHost_Value}")
        // println(s"IPC for ${e} test: ${c.io.performance_counters.retired_inst_count.peek().litValue.toDouble / c.io.performance_counters.cycle_count.peek().litValue.toDouble}")
        dut.clock.step()
      }
    }
  }
  val instList_withDmem = Seq(
    "lb", "lbu", "ld", "lh", "lhu", "lw", "lwu",
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
      test(new CoreAndCache(iCacheMemsize = 8192, dCacheMemsize = 8192, tohost = 0x10000000, cpu = classOf[Cpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        dut.clock.setTimeout(1024)
        dut.io.resetVector.poke(0.U)
        dut.io.hartid.poke(0.U)
        fork {
          initialiseImem(s"src/main/resources/rv64ui/${e}_inst.hex", dut)
        } .fork {
          initialiseDmem(s"src/main/resources/rv64ui/${e}_data.hex", dut)
        } .join()

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

  val instList_multiply = Seq(
    "mul", "mulh", "mulhsu", "mulhu", "mulw"
  )
  if(false) {
    instList_multiply.foreach(e => {
      Process(Seq("sh", "-c", s"export PATH=\"$$PATH:/opt/riscv/riscv-gnu-toolchain/bin\" && cd ~/github/HAJIME_Core/src/main/resources/rv64um && sh build_rv64um.sh ${e}")).!
    })
    Process(Seq("sh", "-c", s"cd ~/github/HAJIME_Core/src/main/resources/rv64um && rm *_data.hex")).!
  }
  for(e <- instList_multiply) {
    it should s"pass the test ${e}" in {
      test(new CoreAndCache(iCacheMemsize = 8192, dCacheMemsize = 8192, tohost = 0x10000000, cpu = classOf[Cpu])).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
        dut.clock.setTimeout(1024)
        dut.io.resetVector.poke(0.U)
        dut.io.hartid.poke(0.U)
        initialiseImem(s"src/main/resources/rv64um/${e}_inst.hex", dut)

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

