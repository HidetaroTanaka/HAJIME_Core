package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common._

class VecRegFileReadReq(implicit params: HajimeCoreParams) extends Bundle {
  val sew = UInt(3.W)
  val idx = UInt(log2Up(params.vlen/8).W)
  val vs1 = Input(UInt(5.W))
  val vs2 = Input(UInt(5.W))
  val vd = Input(UInt(5.W))
}

class VecRegFileReadResp(implicit params: HajimeCoreParams) extends Bundle {
  val vs1Out = UInt(params.xprlen.W)
  val vs2Out = UInt(params.xprlen.W)
  val vdOut = UInt(params.xprlen.W)
  val vm = Bool()
}

class VecRegFileReadIO(implicit params: HajimeCoreParams) extends Bundle {
  val req = Input(new VecRegFileReadReq())
  val resp = Output(new VecRegFileReadResp())
}

class VecRegFileWriteReq(implicit params: HajimeCoreParams) extends Bundle {
  val vd = UInt(5.W)
  val sew = UInt(3.W)
  val index = UInt(log2Up(params.vlen/8).W)
  val data = UInt(params.xprlen.W)
}

class VecRegFileIO(vrfPortNum: Int)(implicit params: HajimeCoreParams) extends Bundle {
  val readReq = Vec(vrfPortNum, new VecRegFileReadIO())
  val writeReq = Vec(vrfPortNum, Flipped(ValidIO(new VecRegFileWriteReq())))
  val debug = if(params.debug) Some(Output(Vec(32, UInt(params.vlen.W)))) else None
}

// TODO: make it compatible with LMUL > 1 (bigger number of index)
class VecRegFile(vrfPortNum: Int)(implicit params: HajimeCoreParams) extends Module {
  def readVRF(vrf: Mem[Vec[UInt]], req: VecRegFileReadReq): VecRegFileReadResp = {
    def vecRegToData(vecReg: Vec[UInt], req: VecRegFileReadReq): UInt = {
      MuxLookup(req.sew, 0.U)(
        (0 until 4).map(
          // 0.U -> vs1ReadVecReg(req.idx)
          // 1.U -> Cat(vs1ReadVecReg(req.idx << 1 + 1), vs1ReadVecReg(req.idx << 1))
          // 2.U -> Cat(vs1ReadVecReg(req.idx << 2 + 3), ..., vs1ReadVecReg(req.idx << 2))
          // 3.U -> Cat(vs1ReadVecReg(req.idx << 3 + 7), ..., vs1ReadVecReg(req.idx << 3))
          i => i.U -> Cat((0 until (1 << i)).reverse.map(j => vecReg((req.idx << i).asUInt + j.U)))
        )
      )
    }
    val res = Wire(new VecRegFileReadResp())

    val vs1ReadVecReg: Vec[UInt] = vrf.read(req.vs1)
    val vs2ReadVecReg: Vec[UInt] = vrf.read(req.vs2)
    val vdReadVecReg: Vec[UInt] = vrf.read(req.vd)

    res.vs1Out := vecRegToData(vs1ReadVecReg, req)
    res.vs2Out := vecRegToData(vs2ReadVecReg, req)
    res.vdOut := vecRegToData(vdReadVecReg, req)
    res.vm := vrf.read(0.U)(req.idx.head(req.idx.getWidth-3))(req.idx(2,0))

    res
  }
  /**
   * vrfへの書き込み（1bitマスク書き込み無し）
   * @param vrf ベクタレジスタファイル
   * @param req 書き込み要求
   */
  def writeToVRF(vrf: Mem[Vec[UInt]], req: VecRegFileWriteReq): Unit = {
    val internalWriteData = VecInit((0 until params.vlen / 8).map(_ => 0.U(8.W)))
    val internalWriteMask = VecInit((0 until params.vlen / 8).map(_ => false.B))
    for (i <- 0 until 4) {
      switch(req.sew) {
        is(i.U) {
          for (j <- 0 until (1 << i)) {
            // i=0 (e8) => internalWriteData(io.reqMem.bits.index) := io.reqMem.bits.data(7,0)
            // i=1 (e16) => internalWriteData(io.reqMem.bits.index*2) := io.reqMem.bits.data(7,0)
            //              internalWriteData(io.reqMem.bits.index*2+1) := io.reqMem.bits.data(15,8)
            internalWriteData((req.index << i).asUInt + j.U) := req.data(j * 8 + 7, j * 8)
            internalWriteMask((req.index << i).asUInt + j.U) := true.B
          }
        }
      }
    }
    vrf.write(req.vd, internalWriteData, internalWriteMask)
  }

  val io = IO(new VecRegFileIO(vrfPortNum))

  // vlen[bit]のベクタレジスタ32本
  val vrf = Mem(32, Vec(params.vlen/8, UInt(8.W)))
  // TODO: vdレジスタに書き込む命令が入った時に該当インデックスをfalseに，該当インデックスの0要素目が書き込まれた時にtrueにする
  val vrfReadyTable = RegInit(VecInit((0 until 32).map(_ => true.B)))

  for(readIO <- io.readReq) {
    readIO.resp := readVRF(vrf, readIO.req)
  }

  for(req <- io.writeReq) {
    when(req.valid) {
      writeToVRF(vrf, req.bits)
    }
  }

  if(params.debug) {
    for((d, i) <- io.debug.get.zipWithIndex) {
      d := Cat(vrf.read(i.U).reverse)
    }
  }
}

object VecRegFile extends App {
  implicit val params = HajimeCoreParams()
  def apply(vrfPortNum: Int)(implicit params: HajimeCoreParams): VecRegFile = new VecRegFile(vrfPortNum)
  ChiselStage.emitSystemVerilogFile(VecRegFile(2), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}