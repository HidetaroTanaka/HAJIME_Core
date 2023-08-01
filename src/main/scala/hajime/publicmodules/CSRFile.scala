package hajime.publicmodules

import circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import hajime.common._

class CSRFileReadReq() extends Bundle {
  val csr = UInt(12.W)
}

class CSRFileReadResp(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val data = UInt(xprlen.W)
}

class CSRFileWriteReq(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val csr = UInt(12.W)
  val data = UInt(xprlen.W)
}

class CPUtoCSR(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val cpu_operating = Bool()
  val inst_retire = Bool()
  val hartid = UInt(xprlen.W)
}

class CSRFileIO(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val readReq = Input(new CSRFileReadReq())
  val readResp = Output(new CSRFileReadResp())
  val writeReq = Flipped(ValidIO(new CSRFileWriteReq()))
  val fromCPU = Input(new CPUtoCSR())
}

class CSRFile(implicit params: HajimeCoreParams) extends Module {
  import params._

  val io = IO(new CSRFileIO())

  def zeroInit_Register(): UInt = {
    RegInit(0.U(xprlen.W))
  }

  val cycle = zeroInit_Register()
  val time_counter = RegInit(0.U(log2Up(frequency).W))
  when(io.fromCPU.cpu_operating) {
    cycle := cycle + 1.U(xprlen.W)
    time_counter := time_counter + 1.U(time_counter.getWidth.W)
  }
  val time = zeroInit_Register()
  when(time_counter === frequency.U) {
    time := time + 1.U(xprlen.W)
    time_counter := 0.U
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

  io.readResp.data := MuxLookup(io.readReq.csr, 0.U(xprlen.W))(readOnlyCSRs ++ writableCSRs)
  when(io.writeReq.valid){
    for ((addr, register) <- writableCSRs) {
      when(io.writeReq.bits.csr === addr) {
        register := io.writeReq.bits.data
      }
    }
  }
}
object CSRFile extends App {
  def apply(implicit params: HajimeCoreParams): CSRFile = new CSRFile()
  ChiselStage.emitSystemVerilogFile(apply(HajimeCoreParams()), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}