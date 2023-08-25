package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.VectorInstructions._
import hajime.common._
import hajime.publicmodules._
import chisel3.experimental.BundleLiterals._

class VtypeBundle(implicit params: HajimeCoreParams) extends Bundle {
  val bits = UInt(params.xprlen.W)
  def vill: Bool = bits(params.xprlen-1)
  def vma: Bool = bits(7)
  def vta: Bool = bits(6)
  def vsew: UInt = bits(5,3)
  def vlmul: UInt = bits(2,0)
  def reserved: Bool = bits(params.xprlen-2, 8) =/= 0.U
}

class VecCtrlUnitReq(implicit params: HajimeCoreParams) extends Bundle {
  val vDecode = new VectorDecoderResp
  val rs1_value = UInt(params.xprlen.W)
  val rs2_value = UInt(params.xprlen.W)
  val zimm = UInt(params.xprlen.W)
  val uimm = UInt(params.xprlen.W)
}

class VecCtrlUnitResp(implicit params: HajimeCoreParams) extends Bundle {
  val vtype = new VtypeBundle()
  val vl = UInt(log2Up(params.vlenb).W)
}

class VecCtrlUnitIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Input(Valid(new VecCtrlUnitReq))
  val resp = Output(new VecCtrlUnitResp())
}

class VecCtrlUnit(implicit params: HajimeCoreParams) extends Module with VectorOpConstants {
  val io = IO(new VecCtrlUnitIO())

  val vtype = RegInit((new VtypeBundle()).Lit(
    _.bits -> 0.U
  ))
  val vl = RegInit(0.U(log2Up(params.vlenb).W))
  val avl = Mux(io.req.bits.vDecode.avl_sel === AVL_SEL.RS1.asUInt, io.req.bits.rs1_value, io.req.bits.uimm)
  val vtypeBits = Wire(new VtypeBundle())
  vtypeBits.bits := MuxLookup(io.req.bits.vDecode.vtype_sel, 0.U)(Seq(
    VTYPE_SEL.ZIMM10.asUInt -> Cat(false.B, io.req.bits.zimm(10,0)),
    VTYPE_SEL.ZIMM9.asUInt -> Cat(false.B, io.req.bits.zimm(9,0)),
    VTYPE_SEL.RS2.asUInt -> io.req.bits.rs2_value
  ))
  val usedVectorBits = MuxLookup(vtypeBits.vsew, 0.U)(
    (0 until 4).map(i => i.U(3.W) -> (avl << (3+i).U).asUInt)
  )
  // TODO: add maximum vl for each SEW
  val maxVl = MuxLookup(vtypeBits.vsew, 0.U)(
    // 0 -> Byte (8bits) -> params.vlen / 8 -> params.vlen >> 3
    // 1 -> Halfword (16bits) -> params.vlen / 16
    (0 until 4).map(i => i.U(3.W) -> (params.vlen >> (3+i)).U)
  )

  when(io.req.valid) {
    // non zero LMUL is not supported here
    vtype.bits := Mux(vtypeBits.vill || vtypeBits.reserved || vtypeBits.vlmul =/= 0.U || vtypeBits.vsew(2),
      Cat(true.B, 0.U((params.xprlen-1).W)),
      vtypeBits.bits
    )
    // if avl >= maxVl, then vl = maxVl, else vl = avl
    vl := Mux(avl >= maxVl, maxVl, avl)
  }

  io.resp.vtype := vtype
  io.resp.vl := vl
}

object VecCtrlUnit extends App {
  def apply(implicit params: HajimeCoreParams): VecCtrlUnit = new VecCtrlUnit()
  ChiselStage.emitSystemVerilogFile(VecCtrlUnit(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}