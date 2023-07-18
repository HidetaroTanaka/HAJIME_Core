import chisel3._
import chiseltest._
import org.scalatest.flatspec._

class Core_ApplicationTest extends AnyFlatSpec with ChiselScalatestTester {
  it should "print Hello, World!" in {
    test(new Core_and_cache(icache_hexfilename = "src/main/resources/helloworld_asm_inst.hex", dcache_hexfilename = null)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(1024)
      def get_toHostChar(): Char = {
        dut.io.debug.bits.peekInt().toChar
      }
      def get_toHostValid(): Boolean = {
        dut.io.debug.valid.peekBoolean()
      }
      while(!(get_toHostValid() && (get_toHostChar() == '\u0000'))) {
        dut.clock.step()
        if(get_toHostValid()) {
          print(get_toHostChar())
        }
      }
      dut.io.debug.bits.expect(0.U(64.W))
      dut.io.debug.valid.expect(true.B)
    }
  }
}
