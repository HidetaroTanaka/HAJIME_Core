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

class CSRInterruptReq(implicit params: HajimeCoreParams) extends Bundle {
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
  val interrupt = Flipped(ValidIO(new CSRInterruptReq()))
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

  val readOnlyCSRs: Seq[(UInt, UInt)] = Seq(
    CSRs.cycle.U(12.W) -> cycle,
    CSRs.time.U(12.W) -> time,
    CSRs.instret.U(12.W) -> instret,
    CSRs.mvendorid.U(12.W) -> mvendorid,
    CSRs.marchid.U(12.W) -> marchid,
    CSRs.mimpid.U(12.W) -> mimpid,
    CSRs.mhartid.U(12.W) -> mhartid,
    CSRs.mconfigptr.U(12.W) -> mconfigptr,
  )

  val writableCSRs: Seq[(UInt, UInt)] = Seq(
    CSRs.mstatus.U(12.W) -> mstatus,
    CSRs.misa.U(12.W) -> misa,
    CSRs.medeleg.U(12.W) -> medeleg,
    CSRs.mideleg.U(12.W) -> mideleg,
    CSRs.mie.U(12.W) -> mie,
    CSRs.mtvec.U(12.W) -> mtvec,
    CSRs.mcounteren.U(12.W) -> mcounteren,
    CSRs.mscratch.U(12.W) -> mscratch,
    CSRs.mepc.U(12.W) -> mepc,
    CSRs.mcause.U(12.W) -> mcause,
    CSRs.mtval.U(12.W) -> mtval,
    CSRs.mip.U(12.W) -> mip,
    CSRs.mtinst.U(12.W) -> mtinst,
    CSRs.mtval2.U(12.W) -> mtval2,
    CSRs.mcycle.U(12.W) -> cycle, // Only M-mode can overwrite cycle and instret?
    CSRs.minstret.U(12.W) -> instret,
  )

  // 割り込みの場合はmtvecの下位2bitをクリアしたものを出力（例外ハンドラのアドレス）
  io.readResp.data := Mux(io.interrupt.valid,
    Cat(mtvec.head(xprlen-2), 0.U(2.W)),
    MuxLookup(io.csr_addr, 0.U(xprlen.W))(readOnlyCSRs ++ writableCSRs)
  )
  // 割り込みの場合は書き込まない
  when(io.writeReq.valid && !io.interrupt.valid){
    for ((addr, register) <- writableCSRs) {
      when(io.csr_addr === addr) {
        register := io.writeReq.bits.data
      }
    }
  }

  // 割り込みの場合はmepcに例外発生PC，mcauseに例外要因を書く
  when(io.interrupt.valid) {
    mepc := io.interrupt.bits.mepc_write
    mcause := io.interrupt.bits.mcause_write
  }
}
object CSRFile extends App {
  def apply(implicit params: HajimeCoreParams): CSRFile = new CSRFile()
  ChiselStage.emitSystemVerilogFile(CSRFile(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}