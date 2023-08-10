import chisel3._
import chiseltest._
import org.scalatest.flatspec._

/*
class Core_ApplicationTest extends AnyFlatSpec with ChiselScalatestTester {
  it should "print Hello World in C language" in {
    test(new hajime.simple4Stage.Core_and_cache(icache_hexfilename = "src/main/resources/applications/helloworld_inst.hex", dcache_hexfilename = "src/main/resources/applications/helloworld_data.hex")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(1024)
      def get_toHostChar(): Char = {
        dut.io.toHost.bits.peekInt().toChar
      }
      def get_toHostValid(): Boolean = {
        dut.io.toHost.valid.peekBoolean()
      }
      var toHostWrittenChar: List[Char] = List()
      while (!(get_toHostValid() && (get_toHostChar() == '\u0000'))) {
        dut.clock.step()
        if (get_toHostValid()) {
          // print(get_toHostChar())
          toHostWrittenChar = toHostWrittenChar :+ get_toHostChar()
        }
      }
      dut.io.debug_io.debug_abi_map.a0.expect(0.U(64.W))
      toHostWrittenChar.foreach(print)
      println()
      println(s"IPC for HelloWorld test: ${dut.io.performance_counters.retired_inst_count.peek().litValue.toDouble / dut.io.performance_counters.cycle_count.peek().litValue.toDouble}")
    }
  }
  it should "execute median" in {
    test(new hajime.simple4Stage.Core_and_cache(icache_hexfilename = "src/main/resources/applications/median_inst.hex", dcache_hexfilename = "src/main/resources/applications/median_data.hex")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(65536)
      def get_toHostChar(): Char = {
        dut.io.toHost.bits.peekInt().toChar
      }
      def get_toHostValid(): Boolean = {
        dut.io.toHost.valid.peekBoolean()
      }
      var toHostWrittenChar: List[Char] = List()
      while (!(get_toHostValid() && (get_toHostChar() == '\u0000'))) {
        dut.clock.step()
        if (get_toHostValid()) {
          // print(get_toHostChar())
          toHostWrittenChar = toHostWrittenChar :+ get_toHostChar()
        }
      }
      dut.io.debug_io.debug_abi_map.a0.expect(0.U(64.W))
      toHostWrittenChar.foreach(print)
      println()
      println(s"IPC for median test: ${dut.io.performance_counters.retired_inst_count.peek().litValue.toDouble / dut.io.performance_counters.cycle_count.peek().litValue.toDouble}")
    }
  }
}
*/
