package hajime.publicmodules

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
    "hC00".U(12.W) -> cycle,
    "hC01".U(12.W) -> time,
    "hC02".U(12.W) -> instret,
    "hF11".U(12.W) -> mvendorid,
    "hF12".U(12.W) -> marchid,
    "hF13".U(12.W) -> mimpid,
    "hF14".U(12.W) -> mhartid,
    "hF15".U(12.W) -> mconfigptr,
  )

  val writableCSRs: Seq[(UInt, UInt)] = Seq(
    "h300".U(12.W) -> mstatus,
    "h301".U(12.W) -> misa,
    "h302".U(12.W) -> medeleg,
    "h303".U(12.W) -> mideleg,
    "h304".U(12.W) -> mie,
    "h305".U(12.W) -> mtvec,
    "h306".U(12.W) -> mcounteren,
    "h340".U(12.W) -> mscratch,
    "h341".U(12.W) -> mepc,
    "h342".U(12.W) -> mcause,
    "h343".U(12.W) -> mtval,
    "h344".U(12.W) -> mip,
    "h34A".U(12.W) -> mtinst,
    "h34B".U(12.W) -> mtval2,
    "hB00".U(12.W) -> cycle, // Only M-mode can overwrite cycle and instret?
    "hB02".U(12.W) -> instret,
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
  (new chisel3.stage.ChiselStage).emitVerilog(apply(HajimeCoreParams()), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}