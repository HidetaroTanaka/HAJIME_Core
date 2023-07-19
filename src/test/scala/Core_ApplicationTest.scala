import chisel3._
import chiseltest._
import org.scalatest.flatspec._

class Core_ApplicationTest extends AnyFlatSpec with ChiselScalatestTester {
  it should "print Hello, World!" in {
    test(new Core_and_cache(icache_hexfilename = "src/main/resources/helloworld_asm_inst.hex", dcache_hexfilename = null)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(1024)
      def get_toHostChar(): Char = {
        dut.io.toHost.bits.peekInt().toChar
      }
      def get_toHostValid(): Boolean = {
        dut.io.toHost.valid.peekBoolean()
      }
      var toHostWrittenChar: List[Char] = List()
      val toHostAnsChar: List[Char] = List('H', 'e', 'l', 'l', 'o', ',', ' ', 'W', 'o', 'r', 'l', 'd', '!', '\u0000')
      while(!(get_toHostValid() && (get_toHostChar() == '\u0000'))) {
        dut.clock.step()
        if(get_toHostValid()) {
          print(get_toHostChar())
          toHostWrittenChar = toHostWrittenChar :+ get_toHostChar()
        }
      }
      assert((toHostWrittenChar zip toHostAnsChar).map(x => (x._1 == x._2)).reduce(_ && _))
    }
  }
  it should "print Hello World in C language" in {
    test(new Core_and_cache(icache_hexfilename = "src/main/resources/applications/helloworld_inst.hex", dcache_hexfilename = "src/main/resources/applications/helloworld_data.hex")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(1024)
      def get_toHostChar(): Char = {
        dut.io.toHost.bits.peekInt().toChar
      }
      def get_toHostValid(): Boolean = {
        dut.io.toHost.valid.peekBoolean()
      }
      var toHostWrittenChar: List[Char] = List()
      // val toHostAnsChar: List[Char] = List('H', 'e', 'l', 'l', 'o', ',', ' ', 'W', 'o', 'r', 'l', 'd', '!', '\u0000')
      while (!(get_toHostValid() && (get_toHostChar() == '\u0000'))) {
        dut.clock.step()
        if (get_toHostValid()) {
          print(get_toHostChar())
          toHostWrittenChar = toHostWrittenChar :+ get_toHostChar()
        }
      }
    }
  }
}
