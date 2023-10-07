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
  def inputVectorDecode(inst: String, vm: Bool, vs1: UInt, vs2: UInt, vd: UInt, vsew: UInt, vl: UInt, dut: VectorLdstUnitWithDcache): Unit = {
    val vectorDecode = instVectorDecode(inst)
    for((d, i) <- dut.io.signalIn.bits.vector.vectorDecode.toList.zipWithIndex) {
      d.poke(vectorDecode(i).U)
    }
    dut.io.signalIn.bits.vector.vectorDecode.vm.poke(vm)
    dut.io.signalIn.bits.vector.vs1.poke(vs1)
    dut.io.signalIn.bits.vector.vs2.poke(vs2)
    dut.io.signalIn.bits.vector.vd.poke(vd)
    dut.io.signalIn.bits.vector.vecConf.vtype.vill.poke(false.B)
    dut.io.signalIn.bits.vector.vecConf.vtype.vsew.poke(vsew)
    dut.io.signalIn.bits.vector.vecConf.vtype.vlmul.poke(0.U)
    dut.io.signalIn.bits.vector.vecConf.vl.poke(vl)
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
      // ID: lw, EX: sh, WB: lb
      inputScalarDecode(inst = "lw", rs1Value = 0x3FE0.U, rs2Value = 0.U, immediate = 0x20.U, dut = dut)
      dut.io.scalarResp.valid.expect(true.B)
      dut.io.scalarResp.bits.data.expect("h0000000000000034".U)
      dut.clock.step()
      // EX: lw, WB: sh
      dut.io.signalIn.valid.poke(false.B)
      dut.clock.step()
      // WB: lw
      dut.io.scalarResp.valid.expect(true.B)
      dut.io.scalarResp.bits.data.expect("h00001919".U)
      dut.clock.step()
      // no
      dut.io.scalarResp.valid.expect(false.B)
      dut.clock.step()
      // ID: vse8.v (vl = 32, no mask)
      inputScalarDecode(inst = "vse8.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vse8.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 5.U, vsew = 0.U, vl = 32.U, dut = dut)
      dut.clock.step()
      // EX: vse8.v idx=0-31
      dut.io.signalIn.valid.poke(false.B)
      for(i <- 0 until 32) {
        dut.io.readVrf.resp.vdOut.poke(i.U)
        dut.clock.step()
      }
      // WB: vse8.v
      dut.clock.step()
    }
  }
}
