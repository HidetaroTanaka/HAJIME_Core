package hajime.publicmodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common._

class CSRFileReadResp(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val data = UInt(xprlen.W)
}

class CSRFileWriteReq(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val data = UInt(xprlen.W)
}

class CSRExceptionReq(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val mepc_write = UInt(xprlen.W)
  val mcause_write = UInt(xprlen.W)
}

class CPUtoCSR(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val cpu_operating = Bool()
  val inst_retire = Bool()
  val hartid = UInt(xprlen.W)
}

class CSRFileIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val csr_addr = Input(UInt(12.W))
  val readResp = Output(new CSRFileReadResp())
  val writeReq = Flipped(ValidIO(new CSRFileWriteReq()))
  val fromCPU = Input(new CPUtoCSR())
  val exception = Flipped(ValidIO(new CSRExceptionReq()))
  val vectorCsrPorts = if(params.useVector) Some(Input(new Bundle {
    val vtype = UInt(64.W)
    val vl    = UInt(log2Up(params.vlenb).W)
  })) else None
}

class CSRFile(implicit params: HajimeCoreParams) extends Module {
  import params._

  val io = IO(new CSRFileIO())

  def zeroInit_Register(): UInt = {
    RegInit(0.U(xprlen.W))
  }

  val cycle = zeroInit_Register()
  val (_, counterWrap) = Counter(true.B, frequency)
  when(io.fromCPU.cpu_operating) {
    cycle := cycle + 1.U(xprlen.W)
  }
  val time = zeroInit_Register()
  when(counterWrap) {
    time := time + 1.U(xprlen.W)
  }
  val instret = zeroInit_Register()
  when(io.fromCPU.inst_retire) {
    instret := instret + 1.U(xprlen.W)
  }
  val mvendorid = "h426F79734C6F7665".U(xprlen.W)
  val marchid = "h53686F7461636F6E".U(xprlen.W)
  val mimpid = "h0001145141919810".U(xprlen.W)
  val mhartid = io.fromCPU.hartid
  val mconfigptr = 0.U(xprlen.W)
  val mstatus = zeroInit_Register()
  val misa = RegInit(generateDefaultMISA)
  val medeleg = zeroInit_Register()
  val mideleg = zeroInit_Register()
  val mie = zeroInit_Register()
  val mtvec = zeroInit_Register()
  val mcounteren = zeroInit_Register()
  val mscratch = zeroInit_Register()
  val mepc = zeroInit_Register()
  val mcause = zeroInit_Register()
  val mtval = zeroInit_Register()
  val mip = zeroInit_Register()
  val mtinst = zeroInit_Register()
  val mtval2 = zeroInit_Register()

  /*
  // TODO: Vector Trap and Fixed-Point
  val vstart = if(params.useVector) Some(zeroInit_Register()) else None
  val vxsat = if(params.useVector) Some(zeroInit_Register()) else None
  val vxrm = if(params.useVector) Some(zeroInit_Register()) else None
  val vcsr = if(params.useVector) Some(zeroInit_Register()) else None
   */
  // vl and vtype can be read from EX_WB register
  // val vl = if(params.useVector) Some(RegInit(0.U(log2Up(params.vlenb).W))) else None
  // val vtype = if(params.useVector) Some(zeroInit_Register()) else None
  val vlenb = if(params.useVector) Some(params.vlenb.U) else None

  val readOnlyCSRs: Seq[(Int, UInt)] = Seq(
    CSRs.cycle -> cycle,
    CSRs.time -> time,
    CSRs.instret -> instret,
    CSRs.mvendorid -> mvendorid,
    CSRs.marchid -> marchid,
    CSRs.mimpid -> mimpid,
    CSRs.mhartid -> mhartid,
    CSRs.mconfigptr -> mconfigptr,
  ) ++ (if(params.useVector) {
    Seq(
      CSRs.vl -> io.vectorCsrPorts.get.vl,
      CSRs.vtype -> io.vectorCsrPorts.get.vtype,
      CSRs.vlenb -> vlenb.get,
    )
  } else Nil)

  val writableCSRs: Seq[(Int, UInt)] = Seq(
    CSRs.mstatus -> mstatus,
    CSRs.misa -> misa,
    CSRs.medeleg -> medeleg,
    CSRs.mideleg -> mideleg,
    CSRs.mie -> mie,
    CSRs.mtvec -> mtvec,
    CSRs.mcounteren -> mcounteren,
    CSRs.mscratch -> mscratch,
    CSRs.mepc -> mepc,
    CSRs.mcause -> mcause,
    CSRs.mtval -> mtval,
    CSRs.mip -> mip,
    CSRs.mtinst -> mtinst,
    CSRs.mtval2 -> mtval2,
    CSRs.mcycle -> cycle, // Only M-mode can overwrite cycle and instret?
    CSRs.minstret -> instret,
  )

  // mepcレジスタは書き込み時に下位2bitが0になっていることが保証されているのでそのまま出力する
  io.readResp.data := MuxLookup(io.csr_addr, 0.U(xprlen.W))(
    (readOnlyCSRs ++ writableCSRs).map {
      case (addr, csr) => (addr.U(12.W), csr)
    }
  )
  // 割り込みの場合は書き込まない
  when(io.writeReq.valid && !io.exception.valid){
    for ((addr, csr) <- writableCSRs) {
      when(io.csr_addr === addr.U(12.W)) {
        if(addr == CSRs.mepc) {
          csr := Cat(io.writeReq.bits.data.head(xprlen-2), "b00".U(2.W))
        } else {
          csr := io.writeReq.bits.data
        }
      }
    }
  }

  // 割り込みの場合はmepcに例外発生PC，mcauseに例外要因を書く
  // また，mtvecを読んでPCに書き込む
  when(io.exception.valid) {
    // 例外発生PCの下位2bitは0であることが保証されている
    mepc := io.exception.bits.mepc_write
    mcause := io.exception.bits.mcause_write
    io.readResp.data := Cat(mtvec.head(xprlen-2), 0.U(2.W))
  }
}
object CSRFile extends App {
  def apply(implicit params: HajimeCoreParams): CSRFile = new CSRFile()
  ChiselStage.emitSystemVerilogFile(CSRFile(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}