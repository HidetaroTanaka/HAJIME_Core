package hajime.publicmodules

import chisel3._
import chiseltest._
import hajime.common.Instructions._
import hajime.common._
import org.scalatest.flatspec._

class ALUTest extends AnyFlatSpec with ChiselScalatestTester with ScalarOpConstants {
  import ContentValid._
  it should "not act sussy" in {
    // wtf? I have never curried ALU.apply
    test(new ALU()(HajimeCoreParams())).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      def instDecode(inst: String): List[Int] = {
        inst match {
          case "add" => List(ARITHMETIC_FCN.ADDSUB.litValue.toInt, 0, 0)
          case "sub" => List(ARITHMETIC_FCN.ADDSUB.litValue.toInt, 1, 0)
          case "sll" => List(ARITHMETIC_FCN.SLL.litValue.toInt, 0, 0)
          case "slt" => List(ARITHMETIC_FCN.SLT.litValue.toInt, 0, 0)
          case "sltu" => List(ARITHMETIC_FCN.SLTU.litValue.toInt, 0, 0)
          case "xor" => List(ARITHMETIC_FCN.XOR.litValue.toInt, 0, 0)
          case "srl" => List(ARITHMETIC_FCN.SR.litValue.toInt, 0, 0)
          case "sra" => List(ARITHMETIC_FCN.SR.litValue.toInt, 1, 0)
          case "or" => List(ARITHMETIC_FCN.OR.litValue.toInt, 0, 0)
          case "and" => List(ARITHMETIC_FCN.AND.litValue.toInt, 0, 0)
          case "addw" => List(ARITHMETIC_FCN.ADDSUB.litValue.toInt, 0, 1)
          case "subw" => List(ARITHMETIC_FCN.ADDSUB.litValue.toInt, 1, 1)
          case "sllw" => List(ARITHMETIC_FCN.SLL.litValue.toInt, 0, 1)
          case "srlw" => List(ARITHMETIC_FCN.SR.litValue.toInt, 0, 1)
          case "sraw" => List(ARITHMETIC_FCN.SR.litValue.toInt, 1, 1)
          case _ => List(ARITHMETIC_FCN.NONE.litValue.toInt, 0, 0)
        }
      }

      def DO_TEST(testNum: BigInt, inst: String, out: String, in1: String, in2: String): Unit = {
        c.io.in1.poke(in1.U(RISCV_Consts.XLEN.W))
        c.io.in2.poke(in2.U(RISCV_Consts.XLEN.W))
        c.io.funct.arithmetic_funct.poke((instDecode(inst)).head)
        c.io.funct.alu_flag.poke((instDecode(inst)(1)))
        c.io.funct.op32.poke((instDecode(inst)(2)))
        c.io.out.expect(out.U(RISCV_Consts.XLEN.W))
        if(c.io.out.peekInt() == out.U.litValue) println(s"test $testNum for $inst passed")
      }

      def TEST_RR_OP(testNum: BigInt, inst: String, out: String, in1: String, in2: String): Unit = {
        DO_TEST(testNum, inst, out, in1, in2)
        c.clock.step()
      }

      TEST_RR_OP(2, "add", "h00000000", "h00000000", "h00000000")
      TEST_RR_OP(3, "add", "h00000002", "h00000001", "h00000001")
      TEST_RR_OP(4, "add", "h0000000a", "h00000003", "h00000007")

      TEST_RR_OP(5, "add", "hffffffffffff8000", "h0000000000000000", "hffffffffffff8000")
      TEST_RR_OP(6, "add", "hffffffff80000000", "hffffffff80000000", "h00000000")
      TEST_RR_OP(7, "add", "hffffffff7fff8000", "hffffffff80000000", "hffffffffffff8000")

      TEST_RR_OP(8, "add", "h0000000000007fff", "h0000000000000000", "h0000000000007fff")
      TEST_RR_OP(9, "add", "h000000007fffffff", "h000000007fffffff", "h0000000000000000")
      TEST_RR_OP(10, "add", "h0000000080007ffe", "h000000007fffffff", "h0000000000007fff")

      TEST_RR_OP(11, "add", "hffffffff80007fff", "hffffffff80000000", "h0000000000007fff")
      TEST_RR_OP(12, "add", "h000000007fff7fff", "h000000007fffffff", "hffffffffffff8000")

      TEST_RR_OP(13, "add", "hffffffffffffffff", "h0000000000000000", "hffffffffffffffff")
      TEST_RR_OP(14, "add", "h0000000000000000", "hffffffffffffffff", "h0000000000000001")
      TEST_RR_OP(15, "add", "hfffffffffffffffe", "hffffffffffffffff", "hffffffffffffffff")

      TEST_RR_OP(16, "add", "h0000000080000000", "h0000000000000001", "h000000007fffffff")


      TEST_RR_OP(2, "addw", "h00000000", "h00000000", "h00000000")
      TEST_RR_OP(3, "addw", "h00000002", "h00000001", "h00000001")
      TEST_RR_OP(4, "addw", "h0000000a", "h00000003", "h00000007")

      TEST_RR_OP(5, "addw", "hffffffffffff8000", "h0000000000000000", "hffffffffffff8000")
      TEST_RR_OP(6, "addw", "hffffffff80000000", "hffffffff80000000", "h00000000")
      TEST_RR_OP(7, "addw", "h000000007fff8000", "hffffffff80000000", "hffffffffffff8000")

      TEST_RR_OP(8, "addw", "h0000000000007fff", "h0000000000000000", "h0000000000007fff")
      TEST_RR_OP(9, "addw", "h000000007fffffff", "h000000007fffffff", "h0000000000000000")
      TEST_RR_OP(10, "addw", "hffffffff80007ffe", "h000000007fffffff", "h0000000000007fff")

      TEST_RR_OP(11, "addw", "hffffffff80007fff", "hffffffff80000000", "h0000000000007fff")
      TEST_RR_OP(12, "addw", "h000000007fff7fff", "h000000007fffffff", "hffffffffffff8000")

      TEST_RR_OP(13, "addw", "hffffffffffffffff", "h0000000000000000", "hffffffffffffffff")
      TEST_RR_OP(14, "addw", "h0000000000000000", "hffffffffffffffff", "h0000000000000001")
      TEST_RR_OP(15, "addw", "hfffffffffffffffe", "hffffffffffffffff", "hffffffffffffffff")

      TEST_RR_OP(16, "addw", "hffffffff80000000", "h0000000000000001", "h000000007fffffff")


      TEST_RR_OP(2, "and", "h0f000f00", "hff00ff00", "h0f0f0f0f")
      TEST_RR_OP(3, "and", "h00f000f0", "h0ff00ff0", "hf0f0f0f0")
      TEST_RR_OP(4, "and", "h000f000f", "h00ff00ff", "h0f0f0f0f")
      TEST_RR_OP(5, "and", "hf000f000", "hf00ff00f", "hf0f0f0f0")


      TEST_RR_OP(2, "or", "hff0fff0f", "hff00ff00", "h0f0f0f0f")
      TEST_RR_OP(3, "or", "hfff0fff0", "h0ff00ff0", "hf0f0f0f0")
      TEST_RR_OP(4, "or", "h0fff0fff", "h00ff00ff", "h0f0f0f0f")
      TEST_RR_OP(5, "or", "hf0fff0ff", "hf00ff00f", "hf0f0f0f0")


      TEST_RR_OP(2, "sll", "h0000000000000001", "h0000000000000001", "d0")
      TEST_RR_OP(3, "sll", "h0000000000000002", "h0000000000000001", "d1");
      TEST_RR_OP(4, "sll", "h0000000000000080", "h0000000000000001", "d7");
      TEST_RR_OP(5, "sll", "h0000000000004000", "h0000000000000001", "d14");
      TEST_RR_OP(6, "sll", "h0000000080000000", "h0000000000000001", "d31");

      TEST_RR_OP(7, "sll", "hffffffffffffffff", "hffffffffffffffff", "d0");
      TEST_RR_OP(8, "sll", "hfffffffffffffffe", "hffffffffffffffff", "d1");
      TEST_RR_OP(9, "sll", "hffffffffffffff80", "hffffffffffffffff", "d7");
      TEST_RR_OP(10, "sll", "hffffffffffffc000", "hffffffffffffffff", "d14");
      TEST_RR_OP(11, "sll", "hffffffff80000000", "hffffffffffffffff", "d31");

      TEST_RR_OP(12, "sll", "h0000000021212121", "h0000000021212121", "d0");
      TEST_RR_OP(13, "sll", "h0000000042424242", "h0000000021212121", "d1");
      TEST_RR_OP(14, "sll", "h0000001090909080", "h0000000021212121", "d7");
      TEST_RR_OP(15, "sll", "h0000084848484000", "h0000000021212121", "d14");
      TEST_RR_OP(16, "sll", "h1090909080000000", "h0000000021212121", "d31");

      TEST_RR_OP(17, "sll", "h0000000021212121", "h0000000021212121", "hffffffffffffffc0");
      TEST_RR_OP(18, "sll", "h0000000042424242", "h0000000021212121", "hffffffffffffffc1");
      TEST_RR_OP(19, "sll", "h0000001090909080", "h0000000021212121", "hffffffffffffffc7");
      TEST_RR_OP(20, "sll", "h0000084848484000", "h0000000021212121", "hffffffffffffffce");

      TEST_RR_OP(21, "sll", "h8000000000000000", "h0000000021212121", "hffffffffffffffff");
      TEST_RR_OP(50, "sll", "h8000000000000000", "h0000000000000001", "d63");
      TEST_RR_OP(51, "sll", "hffffff8000000000", "hffffffffffffffff", "d39");
      TEST_RR_OP(52, "sll", "h0909080000000000", "h0000000021212121", "d43");


      TEST_RR_OP(2, "sllw", "h0000000000000001", "h0000000000000001", "d0");
      TEST_RR_OP(3, "sllw", "h0000000000000002", "h0000000000000001", "d1");
      TEST_RR_OP(4, "sllw", "h0000000000000080", "h0000000000000001", "d7");
      TEST_RR_OP(5, "sllw", "h0000000000004000", "h0000000000000001", "d14");
      TEST_RR_OP(6, "sllw", "hffffffff80000000", "h0000000000000001", "d31");

      TEST_RR_OP(7, "sllw", "hffffffffffffffff", "hffffffffffffffff", "d0");
      TEST_RR_OP(8, "sllw", "hfffffffffffffffe", "hffffffffffffffff", "d1");
      TEST_RR_OP(9, "sllw", "hffffffffffffff80", "hffffffffffffffff", "d7");
      TEST_RR_OP(10, "sllw", "hffffffffffffc000", "hffffffffffffffff", "d14");
      TEST_RR_OP(11, "sllw", "hffffffff80000000", "hffffffffffffffff", "d31");

      TEST_RR_OP(12, "sllw", "h0000000021212121", "h0000000021212121", "d0");
      TEST_RR_OP(13, "sllw", "h0000000042424242", "h0000000021212121", "d1");
      TEST_RR_OP(14, "sllw", "hffffffff90909080", "h0000000021212121", "d7");
      TEST_RR_OP(15, "sllw", "h0000000048484000", "h0000000021212121", "d14");
      TEST_RR_OP(16, "sllw", "hffffffff80000000", "h0000000021212121", "d31");

      TEST_RR_OP(17, "sllw", "h0000000021212121", "h0000000021212121", "hffffffffffffffe0");
      TEST_RR_OP(18, "sllw", "h0000000042424242", "h0000000021212121", "hffffffffffffffe1");
      TEST_RR_OP(19, "sllw", "hffffffff90909080", "h0000000021212121", "hffffffffffffffe7");
      TEST_RR_OP(20, "sllw", "h0000000048484000", "h0000000021212121", "hffffffffffffffee");
      TEST_RR_OP(21, "sllw", "hffffffff80000000", "h0000000021212121", "hffffffffffffffff");

      TEST_RR_OP(44, "sllw", "h0000000012345678", "hffffffff12345678", "d0");
      TEST_RR_OP(45, "sllw", "h0000000023456780", "hffffffff12345678", "d4");
      TEST_RR_OP(46, "sllw", "hffffffff92345678", "h0000000092345678", "d0");
      TEST_RR_OP(47, "sllw", "hffffffff93456780", "h0000000099345678", "d4");


      TEST_RR_OP(2, "slt", "d0", "h0000000000000000", "h0000000000000000")
      TEST_RR_OP(3, "slt", "d0", "h0000000000000001", "h0000000000000001")
      TEST_RR_OP(4, "slt", "d1", "h0000000000000003", "h0000000000000007")
      TEST_RR_OP(5, "slt", "d0", "h0000000000000007", "h0000000000000003")

      TEST_RR_OP(6, "slt", "d0", "h0000000000000000", "hffffffffffff8000")
      TEST_RR_OP(7, "slt", "d1", "hffffffff80000000", "h0000000000000000")
      TEST_RR_OP(8, "slt", "d1", "hffffffff80000000", "hffffffffffff8000")

      TEST_RR_OP(9, "slt", "d1", "h0000000000000000", "h0000000000007fff")
      TEST_RR_OP(10, "slt", "d0", "h000000007fffffff", "h0000000000000000")
      TEST_RR_OP(11, "slt", "d0", "h000000007fffffff", "h0000000000007fff")

      TEST_RR_OP(12, "slt", "d1", "hffffffff80000000", "h0000000000007fff")
      TEST_RR_OP(13, "slt", "d0", "h000000007fffffff", "hffffffffffff8000")

      TEST_RR_OP(14, "slt", "d0", "h0000000000000000", "hffffffffffffffff")
      TEST_RR_OP(15, "slt", "d1", "hffffffffffffffff", "h0000000000000001")
      TEST_RR_OP(16, "slt", "d0", "hffffffffffffffff", "hffffffffffffffff")


      TEST_RR_OP(2, "sltu", "d0", "h00000000", "h00000000")
      TEST_RR_OP(3, "sltu", "d0", "h00000001", "h00000001")
      TEST_RR_OP(4, "sltu", "d1", "h00000003", "h00000007")
      TEST_RR_OP(5, "sltu", "d0", "h00000007", "h00000003")

      TEST_RR_OP(6, "sltu", "d1", "h00000000", "hffff8000")
      TEST_RR_OP(7, "sltu", "d0", "h80000000", "h00000000")
      TEST_RR_OP(8, "sltu", "d1", "h80000000", "hffff8000")

      TEST_RR_OP(9, "sltu", "d1", "h00000000", "h00007fff")
      TEST_RR_OP(10, "sltu", "d0", "h7fffffff", "h00000000")
      TEST_RR_OP(11, "sltu", "d0", "h7fffffff", "h00007fff")

      TEST_RR_OP(12, "sltu", "d0", "h80000000", "h00007fff")
      TEST_RR_OP(13, "sltu", "d1", "h7fffffff", "hffff8000")

      TEST_RR_OP(14, "sltu", "d1", "h00000000", "hffffffff")
      TEST_RR_OP(15, "sltu", "d0", "hffffffff", "h00000001")
      TEST_RR_OP(16, "sltu", "d0", "hffffffff", "hffffffff")


      TEST_RR_OP(2, "sra", "hffffffff80000000", "hffffffff80000000", "d0");
      TEST_RR_OP(3, "sra", "hffffffffc0000000", "hffffffff80000000", "d1");
      TEST_RR_OP(4, "sra", "hffffffffff000000", "hffffffff80000000", "d7");
      TEST_RR_OP(5, "sra", "hfffffffffffe0000", "hffffffff80000000", "d14");
      TEST_RR_OP(6, "sra", "hffffffffffffffff", "hffffffff80000001", "d31");

      TEST_RR_OP(7, "sra", "h000000007fffffff", "h000000007fffffff", "d0");
      TEST_RR_OP(8, "sra", "h000000003fffffff", "h000000007fffffff", "d1");
      TEST_RR_OP(9, "sra", "h0000000000ffffff", "h000000007fffffff", "d7");
      TEST_RR_OP(10, "sra", "h000000000001ffff", "h000000007fffffff", "d14");
      TEST_RR_OP(11, "sra", "h0000000000000000", "h000000007fffffff", "d31");

      TEST_RR_OP(12, "sra", "hffffffff81818181", "hffffffff81818181", "d0");
      TEST_RR_OP(13, "sra", "hffffffffc0c0c0c0", "hffffffff81818181", "d1");
      TEST_RR_OP(14, "sra", "hffffffffff030303", "hffffffff81818181", "d7");
      TEST_RR_OP(15, "sra", "hfffffffffffe0606", "hffffffff81818181", "d14");
      TEST_RR_OP(16, "sra", "hffffffffffffffff", "hffffffff81818181", "d31");

      TEST_RR_OP(17, "sra", "hffffffff81818181", "hffffffff81818181", "hffffffffffffffc0");
      TEST_RR_OP(18, "sra", "hffffffffc0c0c0c0", "hffffffff81818181", "hffffffffffffffc1");
      TEST_RR_OP(19, "sra", "hffffffffff030303", "hffffffff81818181", "hffffffffffffffc7");
      TEST_RR_OP(20, "sra", "hfffffffffffe0606", "hffffffff81818181", "hffffffffffffffce");
      TEST_RR_OP(21, "sra", "hffffffffffffffff", "hffffffff81818181", "hffffffffffffffff");


      TEST_RR_OP(2, "sraw", "hffffffff80000000", "hffffffff80000000", "d0");
      TEST_RR_OP(3, "sraw", "hffffffffc0000000", "hffffffff80000000", "d1");
      TEST_RR_OP(4, "sraw", "hffffffffff000000", "hffffffff80000000", "d7");
      TEST_RR_OP(5, "sraw", "hfffffffffffe0000", "hffffffff80000000", "d14");
      TEST_RR_OP(6, "sraw", "hffffffffffffffff", "hffffffff80000001", "d31");

      TEST_RR_OP(7, "sraw", "h000000007fffffff", "h000000007fffffff", "d0");
      TEST_RR_OP(8, "sraw", "h000000003fffffff", "h000000007fffffff", "d1");
      TEST_RR_OP(9, "sraw", "h0000000000ffffff", "h000000007fffffff", "d7");
      TEST_RR_OP(10, "sraw", "h000000000001ffff", "h000000007fffffff", "d14");
      TEST_RR_OP(11, "sraw", "h0000000000000000", "h000000007fffffff", "d31");

      TEST_RR_OP(12, "sraw", "hffffffff81818181", "hffffffff81818181", "d0");
      TEST_RR_OP(13, "sraw", "hffffffffc0c0c0c0", "hffffffff81818181", "d1");
      TEST_RR_OP(14, "sraw", "hffffffffff030303", "hffffffff81818181", "d7");
      TEST_RR_OP(15, "sraw", "hfffffffffffe0606", "hffffffff81818181", "d14");
      TEST_RR_OP(16, "sraw", "hffffffffffffffff", "hffffffff81818181", "d31");

      TEST_RR_OP(17, "sraw", "hffffffff81818181", "hffffffff81818181", "hffffffffffffffe0");
      TEST_RR_OP(18, "sraw", "hffffffffc0c0c0c0", "hffffffff81818181", "hffffffffffffffe1");
      TEST_RR_OP(19, "sraw", "hffffffffff030303", "hffffffff81818181", "hffffffffffffffe7");
      TEST_RR_OP(20, "sraw", "hfffffffffffe0606", "hffffffff81818181", "hffffffffffffffee");
      TEST_RR_OP(21, "sraw", "hffffffffffffffff", "hffffffff81818181", "hffffffffffffffff");

      TEST_RR_OP(44, "sraw", "h0000000012345678", "hffffffff12345678", "d0");
      TEST_RR_OP(45, "sraw", "h0000000001234567", "hffffffff12345678", "d4");
      TEST_RR_OP(46, "sraw", "hffffffff92345678", "h0000000092345678", "d0");
      TEST_RR_OP(47, "sraw", "hfffffffff9234567", "h0000000092345678", "d4");


      TEST_RR_OP(2, "srl", "hffffffff80000000", "hffffffff80000000", "d0")
      TEST_RR_OP(3, "srl", "h7fffffffc0000000", "hffffffff80000000", "d1")
      TEST_RR_OP(4, "srl", "h01FFFFFFFF000000", "hffffffff80000000", "d7")
      TEST_RR_OP(5, "srl", "h0003FFFFFFFE0000", "hffffffff80000000", "d14")
      TEST_RR_OP(6, "srl", "h00000001FFFFFFFF", "hffffffff80000001", "d31")

      TEST_RR_OP(2, "srl", "hffffffffffffffff", "hffffffffffffffff", "d0")
      TEST_RR_OP(3, "srl", "h7fffffffffffffff", "hffffffffffffffff", "d1")
      TEST_RR_OP(4, "srl", "h01ffffffffffffff", "hffffffffffffffff", "d7")
      TEST_RR_OP(5, "srl", "h0003ffffffffffff", "hffffffffffffffff", "d14")
      TEST_RR_OP(6, "srl", "h00000001ffffffff", "hffffffffffffffff", "d31")

      TEST_RR_OP(2, "srl", "h0000000021212121", "h0000000021212121", "d0")
      TEST_RR_OP(3, "srl", "h0000000010909090", "h0000000021212121", "d1")
      TEST_RR_OP(4, "srl", "h0000000000424242", "h0000000021212121", "d7")
      TEST_RR_OP(5, "srl", "h0000000000008484", "h0000000021212121", "d14")
      TEST_RR_OP(6, "srl", "h0000000000000000", "h0000000021212121", "d31")

      TEST_RR_OP(17, "srl", "h0000000021212121", "h0000000021212121", "hffffffffffffffc0")
      TEST_RR_OP(18, "srl", "h0000000010909090", "h0000000021212121", "hffffffffffffffc1")
      TEST_RR_OP(19, "srl", "h0000000000424242", "h0000000021212121", "hffffffffffffffc7")
      TEST_RR_OP(20, "srl", "h0000000000008484", "h0000000021212121", "hffffffffffffffce")
      TEST_RR_OP(21, "srl", "h0000000000000000", "h0000000021212121", "hffffffffffffffff")


      TEST_RR_OP(2, "srlw", "hffffffff80000000", "hffffffff80000000", "d0");
      TEST_RR_OP(3, "srlw", "h0000000040000000", "hffffffff80000000", "d1");
      TEST_RR_OP(4, "srlw", "h0000000001000000", "hffffffff80000000", "d7");
      TEST_RR_OP(5, "srlw", "h0000000000020000", "hffffffff80000000", "d14");
      TEST_RR_OP(6, "srlw", "h0000000000000001", "hffffffff80000001", "d31");

      TEST_RR_OP(7, "srlw", "hffffffffffffffff", "hffffffffffffffff", "d0");
      TEST_RR_OP(8, "srlw", "h000000007fffffff", "hffffffffffffffff", "d1");
      TEST_RR_OP(9, "srlw", "h0000000001ffffff", "hffffffffffffffff", "d7");
      TEST_RR_OP(10, "srlw", "h000000000003ffff", "hffffffffffffffff", "d14");
      TEST_RR_OP(11, "srlw", "h0000000000000001", "hffffffffffffffff", "d31");

      TEST_RR_OP(12, "srlw", "h0000000021212121", "h0000000021212121", "d0");
      TEST_RR_OP(13, "srlw", "h0000000010909090", "h0000000021212121", "d1");
      TEST_RR_OP(14, "srlw", "h0000000000424242", "h0000000021212121", "d7");
      TEST_RR_OP(15, "srlw", "h0000000000008484", "h0000000021212121", "d14");
      TEST_RR_OP(16, "srlw", "h0000000000000000", "h0000000021212121", "d31");

      TEST_RR_OP(17, "srlw", "h0000000021212121", "h0000000021212121", "hffffffffffffffe0");
      TEST_RR_OP(18, "srlw", "h0000000010909090", "h0000000021212121", "hffffffffffffffe1");
      TEST_RR_OP(19, "srlw", "h0000000000424242", "h0000000021212121", "hffffffffffffffe7");
      TEST_RR_OP(20, "srlw", "h0000000000008484", "h0000000021212121", "hffffffffffffffee");
      TEST_RR_OP(21, "srlw", "h0000000000000000", "h0000000021212121", "hffffffffffffffff");

      TEST_RR_OP(44, "srlw", "h0000000012345678", "hffffffff12345678", "d0");
      TEST_RR_OP(45, "srlw", "h0000000001234567", "hffffffff12345678", "d4");
      TEST_RR_OP(46, "srlw", "hffffffff92345678", "h0000000092345678", "d0");
      TEST_RR_OP(47, "srlw", "h0000000009234567", "h0000000092345678", "d4");

      TEST_RR_OP(2, "sub", "h0000000000000000", "h0000000000000000", "h0000000000000000")
      TEST_RR_OP(3, "sub", "h0000000000000000", "h0000000000000001", "h0000000000000001")
      TEST_RR_OP(4, "sub", "hfffffffffffffffc", "h0000000000000003", "h0000000000000007")

      TEST_RR_OP(5, "sub", "h0000000000008000", "h0000000000000000", "hffffffffffff8000")
      TEST_RR_OP(6, "sub", "hffffffff80000000", "hffffffff80000000", "h0000000000000000")
      TEST_RR_OP(7, "sub", "hffffffff80008000", "hffffffff80000000", "hffffffffffff8000")

      TEST_RR_OP(8, "sub", "hffffffffffff8001", "h0000000000000000", "h0000000000007fff")
      TEST_RR_OP(9, "sub", "h000000007fffffff", "h000000007fffffff", "h0000000000000000")
      TEST_RR_OP(10, "sub", "h000000007fff8000", "h000000007fffffff", "h0000000000007fff")

      TEST_RR_OP(11, "sub", "hffffffff7fff8001", "hffffffff80000000", "h0000000000007fff")
      TEST_RR_OP(12, "sub", "h0000000080007fff", "h000000007fffffff", "hffffffffffff8000")

      TEST_RR_OP(13, "sub", "h0000000000000001", "h0000000000000000", "hffffffffffffffff")
      TEST_RR_OP(14, "sub", "hfffffffffffffffe", "hffffffffffffffff", "h0000000000000001")
      TEST_RR_OP(15, "sub", "h0000000000000000", "hffffffffffffffff", "hffffffffffffffff")


      TEST_RR_OP(2, "subw", "h0000000000000000", "h0000000000000000", "h0000000000000000")
      TEST_RR_OP(3, "subw", "h0000000000000000", "h0000000000000001", "h0000000000000001")
      TEST_RR_OP(4, "subw", "hfffffffffffffffc", "h0000000000000003", "h0000000000000007")

      TEST_RR_OP(5, "subw", "h0000000000008000", "h0000000000000000", "hffffffffffff8000")
      TEST_RR_OP(6, "subw", "hffffffff80000000", "hffffffff80000000", "h0000000000000000")
      TEST_RR_OP(7, "subw", "hffffffff80008000", "hffffffff80000000", "hffffffffffff8000")

      TEST_RR_OP(8, "subw", "hffffffffffff8001", "h0000000000000000", "h0000000000007fff")
      TEST_RR_OP(9, "subw", "h000000007fffffff", "h000000007fffffff", "h0000000000000000")
      TEST_RR_OP(10, "subw", "h000000007fff8000", "h000000007fffffff", "h0000000000007fff")

      TEST_RR_OP(11, "subw", "h000000007fff8001", "hffffffff80000000", "h0000000000007fff")
      TEST_RR_OP(12, "subw", "hffffffff80007fff", "h000000007fffffff", "hffffffffffff8000")

      TEST_RR_OP(13, "subw", "h0000000000000001", "h0000000000000000", "hffffffffffffffff")
      TEST_RR_OP(14, "subw", "hfffffffffffffffe", "hffffffffffffffff", "h0000000000000001")
      TEST_RR_OP(15, "subw", "h0000000000000000", "hffffffffffffffff", "hffffffffffffffff")


      TEST_RR_OP(2, "xor", "hf00ff00f", "hff00ff00", "h0f0f0f0f")
      TEST_RR_OP(3, "xor", "hff00ff00", "h0ff00ff0", "hf0f0f0f0")
      TEST_RR_OP(4, "xor", "h0ff00ff0", "h00ff00ff", "h0f0f0f0f")
      TEST_RR_OP(5, "xor", "h00ff00ff", "hf00ff00f", "hf0f0f0f0")
    }
  }
}
