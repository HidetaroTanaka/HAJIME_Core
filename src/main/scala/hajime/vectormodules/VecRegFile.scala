package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common._

class VecRegFileReq(implicit params: HajimeCoreParams) extends Bundle {
  val vd = UInt(5.W)
  val sew = UInt(3.W)
  val index = UInt(log2Up(params.vlen/8).W)
  val data = UInt(params.xprlen.W)
  val vm = Bool()
}

class VecRegFileIO(implicit params: HajimeCoreParams) extends Bundle {
  val sew = Input(UInt(3.W))
  val readIndex = Input(UInt(log2Up(params.vlen/8).W))
  val vs1 = Input(UInt(5.W))
  val vs1Out = Output(UInt(params.xprlen.W))
  val vs2 = Input(UInt(5.W))
  val vs2Out = Output(UInt(params.xprlen.W))
  val vm = Output(Bool())
  val req = Flipped(ValidIO(new VecRegFileReq()))
}

// TODO: make it compatible with actual Mask Register Layout (required for LMUL > 1), retaining 8 masks should be possible
class VecRegFile(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new VecRegFileIO())

  // vlen[bit]のベクタレジスタ32本
  val vrf = Mem(32, Vec(params.vlen/8, UInt(8.W)))

  // マスク書き込み用レジスタ
  // 11.8章 Vector Integer Compare Instructionsを考えると，1サイクル毎の1bitの結果を保持する必要あり
  // 15章 Vector Mask Instructionsはマスク無しでの全ベクタレジスタでのe64での演算とみなせば良い
  val maskWriteReg = RegInit(VecInit((0 until 8).map(_ => false.B)))

  val vs1ReadVecReg: Vec[UInt] = vrf.read(io.vs1)
  val vs2ReadVecReg: Vec[UInt] = vrf.read(io.vs2)
  io.vs1Out := MuxLookup(io.sew, 0.U)(
    (0 until 4).map(
      // vs1ReadVecReg = vrf.read(io.vs1)
      // 0.U -> vs1ReadVecReg(io.readIndex)
      // 1.U -> Cat(vs1ReadVecReg(io.readIndex << 1 + 1), vs1ReadVecReg(io.readIndex << 1))
      // 2.U -> Cat(vs1ReadVecReg(io.readIndex << 2 + 3), ..., vs1ReadVecReg(io.readIndex << 2))
      // 3.U -> Cat(vs1ReadVecReg(io.readIndex << 3 + 7), ..., vs1ReadVecReg(io.readIndex << 3))
      i => i.U -> Cat((0 until (1 << i)).reverse.map(j => vs1ReadVecReg((io.readIndex << i).asUInt + j.U)))
    )
  )
  io.vs2Out := MuxLookup(io.sew, 0.U)(
    (0 until 4).map(
      i => i.U -> Cat((0 until (1 << i)).reverse.map(j => vs2ReadVecReg((io.readIndex << i).asUInt + j.U)))
    )
  )

  io.vm := vrf.read(0.U)(io.readIndex.head(io.readIndex.getWidth-3))(io.readIndex(2,0))

  when(io.req.valid) {
    maskWriteReg(io.req.bits.index(2,0)) := io.req.bits.data(0)
    val internalWriteData = VecInit((0 until params.vlen/8).map(_ => 0.U(8.W)))
    val internalWriteMask = VecInit((0 until params.vlen/8).map(_ => false.B))
    when(io.req.bits.vm && (io.req.bits.index(2,0) === 7.U)) {
      internalWriteData(io.req.bits.index) := Cat(
        (0 until 8).reverse.map {
          case 7 => io.req.bits.data(0)
          case i => maskWriteReg(i)
        }
      )
      internalWriteMask(io.req.bits.index) := true.B
    } .otherwise {
      for(i <- 0 until 4) {
        switch(io.req.bits.sew) {
          is(i.U) {
            for(j <- 0 until (1 << i)) {
              // i=0 (e8) => internalWriteData(io.req.bits.index) := io.req.bits.data(7,0)
              // i=1 (e16) => internalWriteData(io.req.bits.index*2) := io.req.bits.data(7,0)
              //              internalWriteData(io.req.bits.index*2+1) := io.req.bits.data(15,8)
              internalWriteData((io.req.bits.index << i).asUInt + j.U) := io.req.bits.data(j*8+7, j*8)
              internalWriteMask((io.req.bits.index << i).asUInt + j.U) := true.B
            }
          }
        }
      }
    }
    vrf.write(io.req.bits.vd, internalWriteData, internalWriteMask)
  }
}

object VecRegFile extends App {
  def apply(implicit params: HajimeCoreParams): VecRegFile = new VecRegFile()
  ChiselStage.emitSystemVerilogFile(VecRegFile(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}