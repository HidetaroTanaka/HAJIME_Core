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
      case "vlm.v" => List(Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.B, N, CSR_FCN.N, N, Y)
      case "vsm.v" => List (Y, Branch.NONE, Value1.RS1, Value2.I_IMM, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.B, N, CSR_FCN.N, N, Y)
      case "vlse8.v" => List(Y, Branch.NONE, Value1.RS1, Value2.RS2, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.B, N, CSR_FCN.N, N, Y)
      case "vlse16.v" => List (Y, Branch.NONE, Value1.RS1, Value2.RS2, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.H, N, CSR_FCN.N, N, Y)
      case "vlse32.v" => List (Y, Branch.NONE, Value1.RS1, Value2.RS2, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.W, N, CSR_FCN.N, N, Y)
      case "vlse64.v" => List (Y, Branch.NONE, Value1.RS1, Value2.RS2, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.D, N, CSR_FCN.N, N, Y)
      case "vsse8.v" => List (Y, Branch.NONE, Value1.RS1, Value2.RS2, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.B, N, CSR_FCN.N, N, Y)
      case "vsse16.v" => List (Y, Branch.NONE, Value1.RS1, Value2.RS2, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.H, N, CSR_FCN.N, N, Y)
      case "vsse32.v" => List (Y, Branch.NONE, Value1.RS1, Value2.RS2, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.W, N, CSR_FCN.N, N, Y)
      case "vsse64.v" => List (Y, Branch.NONE, Value1.RS1, Value2.RS2, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.D, N, CSR_FCN.N, N, Y)
      case "vloxei8.v" => List(Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.B, N, CSR_FCN.N, N, Y)
      case "vloxei16.v" => List (Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.H, N, CSR_FCN.N, N, Y)
      case "vloxei32.v" => List (Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.W, N, CSR_FCN.N, N, Y)
      case "vloxei64.v" => List (Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_RD, MEM_LEN.D, N, CSR_FCN.N, N, Y)
      case "vsoxei8.v" => List (Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.B, N, CSR_FCN.N, N, Y)
      case "vsoxei16.v" => List (Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.H, N, CSR_FCN.N, N, Y)
      case "vsoxei32.v" => List (Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.W, N, CSR_FCN.N, N, Y)
      case "vsoxei64.v" => List (Y, Branch.NONE, Value1.RS1, Value2.ZERO, ARITHMETIC_FCN.ADDSUB, N, N, WB_SEL.NONE, MEM_FCN.M_WR, MEM_LEN.D, N, CSR_FCN.N, N, Y)

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
      case "vlm.v" => List(N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.MASK_E8, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vsm.v" => List(N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.UNIT_STRIDE, UMOP.MASK_E8, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vlse8.v" => List(N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.STRIDED, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vlse16.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.STRIDED, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vlse32.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.STRIDED, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vlse64.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.STRIDED, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vsse8.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.STRIDED, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vsse16.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.STRIDED, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vsse32.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.STRIDED, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vsse64.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.STRIDED, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vloxei8.v" => List(N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.IDX_ORDERED, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vloxei16.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.IDX_ORDERED, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vloxei32.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.IDX_ORDERED, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vloxei64.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.IDX_ORDERED, UMOP.NORMAL, Y, VEU_FUN.ADD, VSOURCE.VV)
      case "vsoxei8.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.IDX_ORDERED, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vsoxei16.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.IDX_ORDERED, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vsoxei32.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.IDX_ORDERED, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
      case "vsoxei64.v" => List (N, AVL_SEL.NONE, VTYPE_SEL.NONE, MOP.IDX_ORDERED, UMOP.NORMAL, N, VEU_FUN.ADD, VSOURCE.VV)
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
      // EX: vse8.v idx=0-30
      dut.io.signalIn.valid.poke(false.B)
      for(i <- 0 until 31) {
        dut.io.readVrf.resp.vdOut.poke(i.U)
        dut.clock.step()
      }
      // ID: vle16.v (vl=16, no mask), EX: vse8.v idx=31
      dut.io.readVrf.resp.vdOut.poke(31.U)
      inputScalarDecode(inst = "vle16.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vle16.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 3.U, vsew = 1.U, vl = 16.U, dut = dut)
      dut.clock.step()
      // EX: vle16.v idx=0-15, WB: vse8.v
      dut.io.signalIn.valid.poke(false.B)
      for(_ <- 0 until 16) {
        dut.clock.step()
      }
      // WB: vle16.v
      dut.clock.step()
      // ID: vse8.v (vl = 32, masked)
      inputScalarDecode(inst = "vse8.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vse8.v", vm = false.B, vs1 = 0.U, vs2 = 0.U, vd = 5.U, vsew = 0.U, vl = 32.U, dut = dut)
      dut.clock.step()
      // EX: vse8.v idx=0-30, mask=(0 until 32).map(i => (i%3 == 0).B)
      dut.io.signalIn.valid.poke(false.B)
      for(i <- 0 until 31) {
        dut.io.readVrf.resp.vdOut.poke(0xFF.U)
        // write only when index is 3*n
        dut.io.readVrf.resp.vm.poke((i%3==0).B)
        dut.clock.step()
      }
      // ID: vle8.v (vl = 32, no mask), EX: vse8.v idx=31, mask=false
      dut.io.readVrf.resp.vdOut.poke(0xFF.U)
      dut.io.readVrf.resp.vm.poke(false.B)
      inputScalarDecode(inst = "vle8.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vle8.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 10.U, vsew = 0.U, vl = 32.U, dut = dut)
      dut.clock.step()
      // EX: vle8.v idx=0-31, WB: vse8.v
      dut.io.signalIn.valid.poke(false.B)
      for(_ <- 0 until 32) {
        dut.clock.step()
      }
      // WB: vle8.v
      dut.clock.step()
      // ID: vle8.v (vl = 32, masked)
      inputScalarDecode(inst = "vle8.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vle8.v", vm = false.B, vs1 = 0.U, vs2 = 0.U, vd = 16.U, vsew = 0.U, vl = 32.U, dut = dut)
      dut.clock.step()
      // EX: vle8.v idx=0-31, mask=(0 until 32).map(i => (i%7 != 0).B)
      dut.io.signalIn.valid.poke(false.B)
      for(i <- 0 until 32) {
        // write only when index is not 7*n
        dut.io.readVrf.resp.vm.poke((i%7 != 0).B)
        dut.clock.step()
      }
      // WB: vle8.v
      dut.clock.step()
      // ID: vse64.v (vl = 4, no mask), 0x4000 ~ 0x401F
      inputScalarDecode(inst = "vse64.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vse64.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 13.U, vsew = 3.U, vl = 4.U, dut = dut)
      dut.clock.step()
      // EX: vse64.v idx=0-3
      dut.io.signalIn.valid.poke(false.B)
      for(i <- 0 until 4) {
        val base = 0x0706050403020100L
        val acc = 0x0808080808080808L
        dut.io.readVrf.resp.vdOut.poke((base + acc*i).U)
        dut.clock.step()
      }
      // ID: vlse16.v (vl = 8, stride = 2, no mask), WB: vse64.v
      inputScalarDecode(inst = "vlse16.v", rs1Value = 0x4000.U, rs2Value = 2.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vlse16.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 19.U, vsew = 1.U, vl = 8.U, dut = dut)
      dut.clock.step()
      // EX: vlse16.v, idx=0-7
      dut.io.signalIn.valid.poke(false.B)
      dut.clock.step()
      // idx0: 0x4000 -> 0x0100
      // idx1: 0x4004 -> 0x0504
      // ... idx7: 0x401C -> 0x1D1C
      for(i <- 0 until 8) {
        val base = 0x0100
        val acc =  0x0404
        dut.io.vectorResp.toVRF.bits.data.expect((base + acc*i).U)
        dut.io.vectorResp.toVRF.valid.expect(true.B)
        dut.io.vectorResp.toVRF.bits.writeReq.expect(true.B)
        dut.clock.step()
      }
      // ID: vsse8.v (vl = 10, stride = 3, no mask)
      // 0x4000: FF0201FF
      // 0x4004: 07FF0504
      // 0x4008: 0B0AFF08
      // 0x400C: FF0E0DFF
      // 0x4010: 13FF1110
      // 0x4014: 1716FF14
      // 0x4018: FF1A19FF
      // 0x401C: 1F1E1D1C
      inputScalarDecode(inst = "vsse8.v", rs1Value = 0x4000.U, rs2Value = 3.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vsse8.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 24.U, vsew = 0.U, vl = 10.U, dut = dut)
      dut.clock.step()
      // EX: vsse8.v, idx = 0-8
      dut.io.signalIn.valid.poke(false.B)
      for(_ <- 0 until 9) {
        dut.io.readVrf.resp.vdOut.poke(0xFF.U)
        dut.clock.step()
      }
      // ID: vle64.v (vl = 4, no mask), EX: vsse8.v, idx = 9
      inputScalarDecode(inst = "vle64.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vle64.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 9.U, vsew = 3.U, vl = 4.U, dut = dut)
      dut.io.readVrf.resp.vdOut.poke(0xFF.U)
      dut.clock.step()
      dut.io.signalIn.valid.poke(false.B)
      // EX: vle64.v, idx = 0-3, WB: vsse8.v, vle64.v
      for(i <- 0 until 5) {
        val expectList = List("h07FF0504FF0201FF".U, "hFF0E0DFF0B0AFF08".U, "h1716FF1413FF1110".U, "h1F1E1D1CFF1A19FF".U)
        dut.clock.step()
        if(i != 4) dut.io.vectorResp.toVRF.bits.data.expect(expectList(i))
      }
      // ID: vsse16.v (vl = 6, stride = -3, no mask)
      // 0x4000: FF020000
      // 0x4004: 00000504
      // 0x4008: 0B0AFF08
      // 0x400C: FF0E0000
      // 0x4010: 00001110
      // 0x4014: 1716FF14
      // 0x4018: FF1A0000
      // 0x401C: 00001D1C
      inputScalarDecode(inst = "vsse16.v", rs1Value = 0x401E.U, rs2Value = "hFFFFFFFFFFFFFFFD".U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vsse16.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 15.U, vsew = 1.U, vl = 6.U, dut = dut)
      dut.clock.step()
      dut.io.signalIn.valid.poke(false.B)
      // EX: vsse16.v, idx=0-4
      for(_ <- 0 until 5) {
        dut.io.readVrf.resp.vdOut.poke(0x0000.U)
        dut.clock.step()
      }
      // ID: vle64.v (vl=4, no mask), EX: vsse16.v, idx=5
      inputScalarDecode(inst = "vle64.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vle64.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 11.U, vsew = 3.U, vl = 4.U, dut = dut)
      dut.clock.step()
      // EX: vle64.v, idx=0-3, WB: vle64.v
      dut.io.signalIn.valid.poke(false.B)
      for(i <- 0 until 5) {
        val expectList = List("h00000504FF020000".U, "hFF0E00000B0AFF08".U, "h1716FF1400001110".U, "h00001D1CFF1A0000".U)
        dut.clock.step()
        if(i != 4) dut.io.vectorResp.toVRF.bits.data.expect(expectList(i))
      }
      // ID: vsoxei8.v (vl = 10, no mask)
      // vs2: {28, 1, 31, 20, 23, 11, 13, 25, 24, 4}
      // vs3: {00, 01, 02, 03, 04, 05, 06, 07, 08, 09}
      // 0x4000: FF020100
      // 0x4004: 00000509
      // 0x4008: 050AFF08
      // 0x400C: FF0E0600
      // 0x4010: 00001110
      // 0x4014: 0416FF03
      // 0x4018: FF1A0708
      // 0x401C: 02001D00
      inputScalarDecode(inst = "vsoxei8.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vsoxei8.v", vm = true.B, vs1 = 1.U, vs2 = 2.U, vd = 11.U, vsew = 0.U, vl = 10.U, dut = dut)
      dut.clock.step()
      // EX: vs0xei8.v idx=0-9
      dut.io.signalIn.valid.poke(false.B)
      for(i <- 0 until 10) {
        val vs2Array = List(28, 1, 31, 20, 23, 11, 13, 25, 24, 4).map(_.U)
        val vs3Array = (0 until 10).map(_.U)
        dut.io.readVrf.resp.vs2Out.poke(vs2Array(i))
        dut.io.readVrf.resp.vdOut.poke(vs3Array(i))
        dut.clock.step()
      }
      // ID: vle64.v (vl=4, no mask)
      inputScalarDecode(inst = "vle64.v", rs1Value = 0x4000.U, rs2Value = 0.U, immediate = 0.U, dut = dut)
      inputVectorDecode(inst = "vle64.v", vm = true.B, vs1 = 0.U, vs2 = 0.U, vd = 11.U, vsew = 3.U, vl = 4.U, dut = dut)
      dut.clock.step()
      // EX: vle64.v, idx=0-3, WB: vle64.v
      dut.io.signalIn.valid.poke(false.B)
      for (i <- 0 until 5) {
        val expectList = List("h00000509FF020100".U, "hFF0E0600050AFF08".U, "h0416FF0300001110".U, "h02001D00FF1A0708".U)
        dut.clock.step()
        if (i != 4) dut.io.vectorResp.toVRF.bits.data.expect(expectList(i))
      }
    }
  }
}
