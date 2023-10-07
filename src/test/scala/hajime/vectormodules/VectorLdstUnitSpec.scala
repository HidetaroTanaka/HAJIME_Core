package hajime.vectormodules

import chisel3._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import hajime.axiIO.AXI4liteIO
import hajime.common._
import hajime.publicmodules._
import org.scalatest.flatspec._

import scala.io._

object MemInitializer {
  def initialiseMemWithAxi(filename: String, axi: AXI4liteIO, initialising: Bool, clock: Clock, baseAddr: Int): Unit = {
    axi.ar.bits.addr.poke(0.U)
    axi.ar.bits.prot.poke(0.U)
    axi.ar.valid.poke(false.B)
    axi.aw.bits.addr.poke(0.U)
    axi.aw.bits.prot.poke(0.U)
    axi.aw.valid.poke(false.B)
    axi.b.ready.poke(true.B)
    axi.r.ready.poke(true.B)
    axi.w.bits.data.poke(0.U)
    axi.w.bits.strb.poke(0.U)
    axi.w.valid.poke(false.B)
    initialising.poke(true.B)
    // initialise icache from file
    val fileSource = Source.fromFile(filename)
    for ((data, idx) <- fileSource.getLines().zipWithIndex) {
      axi.aw.bits.addr.poke((idx * 4 + baseAddr).U)
      axi.aw.valid.poke(true.B)
      axi.w.bits.data.poke(s"h$data".U(32.W))
      axi.w.bits.strb.poke(0xF.U(8.W))
      axi.w.valid.poke(true.B)
      clock.step()
    }
    fileSource.close()
    axi.aw.valid.poke(false.B)
    axi.w.valid.poke(false.B)
    initialising.poke(false.B)
  }
}

class VectorLdstUnitSpec extends AnyFlatSpec with ChiselScalatestTester with ScalarOpConstants with VectorOpConstants {
  import ContentValid._
  def instScalarDecode(inst: String): List[Int] = {
    (inst match {
      case "lb" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.MEM, MEM_FCN.M_RD, MEM_LEN.B, Y, CSR_FCN.N, N, N)
      case "lh" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.MEM, MEM_FCN.M_RD, MEM_LEN.H, Y, CSR_FCN.N, N, N)
      case "lw" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.MEM, MEM_FCN.M_RD, MEM_LEN.W, Y, CSR_FCN.N, N, N)
      case "ld" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.MEM, MEM_FCN.M_RD, MEM_LEN.D, Y, CSR_FCN.N, N, N)
      case "lbu" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.MEM, MEM_FCN.M_RD, MEM_LEN.B, N, CSR_FCN.N, N, N)
      case "lwu" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.MEM, MEM_FCN.M_RD, MEM_LEN.W, N, CSR_FCN.N, N, N)
      case "lhu" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.MEM, MEM_FCN.M_RD, MEM_LEN.H, N, CSR_FCN.N, N, N)
      case "sb" => List(Y, Branch.NONE, Value1.RS1, Value2.S_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.B, N, CSR_FCN.N, N, N)
      case "sh" => List (Y, Branch.NONE, Value1.RS1, Value2.S_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.H, N, CSR_FCN.N, N, N)
      case "sw" => List (Y, Branch.NONE, Value1.RS1, Value2.S_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.W, N, CSR_FCN.N, N, N)
      case "sd" => List(Y, Branch.NONE,  Value1.RS1,   Value2.S_IMM, ARITHMETIC_FCN.ADDSUB,  N, N, WB_SEL.NONE,  MEM_FCN.M_WR,   MEM_LEN.D, N, CSR_FCN.N, N, N)
      case "vle8.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.B, N, CSR_FCN.N, N, Y)
      case "vle16.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.H, N, CSR_FCN.N, N, Y)
      case "vle32.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.W, N, CSR_FCN.N, N, Y)
      case "vle64.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.D, N, CSR_FCN.N, N, Y)
      case "vse8.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.B, N, CSR_FCN.N, N, Y)
      case "vse16.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.H, N, CSR_FCN.N, N, Y)
      case "vse32.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.W, N, CSR_FCN.N, N, Y)
      case "vse64.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.D, N, CSR_FCN.N, N, Y)
      case _ => List (N, Branch.NONE, Value1.ZERO, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_NONE, MEM_LEN.B, N, CSR_FCN.N, N, N)
    }).map(_.litValue.toInt)
  }
  def inputScalarDecode(inst: String, rs1Value: UInt, rs2Value: UInt, immediate: UInt, dut: VectorLdstUnitWithDcache): Unit = {
    val scalarDecode = instScalarDecode(inst)
    dut.io.signalIn.valid.poke(scalarDecode(0).B)
    /*
    dut.io.signalIn.bits.scalar.scalarDecode.poke(new ID_output().Lit(
      _.branch -> scalarDecode(1).U,
      _.value1 -> scalarDecode(2).U,
      _.value2 -> scalarDecode(3).U,
      _.arithmetic_funct -> scalarDecode(4).U,
      _.alu_flag -> scalarDecode(5).B,
      _.op32 -> scalarDecode(6).U.asBool,
      _.writeback_selector -> scalarDecode(7).U,
      _.memory_function -> scalarDecode(8).U,
      _.memory_length -> scalarDecode(9).U,
      _.mem_sext -> scalarDecode(10).B,
      _.csr_funct -> scalarDecode(11).U,
      _.fence -> scalarDecode(12).U,
      _.vector.get -> scalarDecode(13).B
    ))
     */
    for((d,i) <- dut.io.signalIn.bits.scalar.scalarDecode.toList zip (1 until 14)) {
      d.poke(scalarDecode(i).U)
    }
    dut.io.signalIn.bits.vector.scalarVal.poke(rs1Value)
    dut.io.signalIn.bits.scalar.rs2Value.poke(rs2Value)
    dut.io.signalIn.bits.scalar.immediate.poke(immediate)
  }
  def instVectorDecode(inst: String): List[Int] = {
    (inst match {
      case "vle8.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vle16.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vle32.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vle64.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vse8.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vse16.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vse32.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vse64.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case _ => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.NONE, UMOP.NONE, N, VEU_FUN.ADD, VSOURCE.VV)
    }).map(_.litValue.toInt)
  }
  def inputVectorDecode(inst: String, vm: Boolean, vs1: Int, vs2: Int, vd: Int, vsew: Int, vl: Int, dut: VectorLdstUnitWithDcache): Unit = {
    val vectorDecode = instVectorDecode(inst)
    /*
    dut.io.signalIn.bits.vector.vectorDecode.poke(new VectorDecoderResp().Lit(
      _.isConfsetInst -> vectorDecode(0).B,
      _.avl_sel -> vectorDecode(1).U,
      _.vtype_sel -> vectorDecode(2).U,
      _.mop -> vectorDecode(3).U,
      _.umop -> vectorDecode(4).U,
      _.vrfWrite -> vectorDecode(5).B,
      _.veuFun -> vectorDecode(6).U,
      _.vSource -> vectorDecode(7).U,
    ))
     */
    // 上のやつはこれでおｋ？
    for((d, i) <- dut.io.signalIn.bits.vector.vectorDecode.toList.zipWithIndex) {
      d.poke(if(i < 8) vectorDecode(i).U else vm.B)
    }
    dut.io.signalIn.bits.vector.vs1.poke(vs1.U)
    dut.io.signalIn.bits.vector.vs2.poke(vs2.U)
    dut.io.signalIn.bits.vector.vd.poke(vd.U)
  }
  it should "vector memory access correctly" in {
    implicit val params: HajimeCoreParams = HajimeCoreParams()
    test(new VectorLdstUnitWithDcache()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      MemInitializer.initialiseMemWithAxi("src/main/resources/applications_rv64i/median_data.hex", dut.dCacheInitialiseIO.bits, dut.dCacheInitialiseIO.valid, dut.clock, 0x4000)
      // ID: lb
      inputScalarDecode(inst = "lb", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0x10.U, dut = dut)
      dut.clock.step()
      // ID: sh, EX: lb
      inputScalarDecode(inst = "sh", rs1Value = 0x4060.U, rs2Value = 0x1919.U, immediate = "hFFFFFFFFFFFFFFA0".U, dut = dut)
      dut.clock.step()
      // EX: sh, WB: lb
      dut.io.signalIn.valid.poke(false.B)
      dut.io.scalarResp.valid.expect(true.B)
      dut.io.scalarResp.bits.data.expect("h0000000100000234".U)
      dut.clock.step()
      // WB: sh
      dut.clock.step()
      // no
      dut.io.scalarResp.valid.expect(false.B)
      dut.clock.step()
    }
  }
}
