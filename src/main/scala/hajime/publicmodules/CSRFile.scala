package hajime.publicmodules

import chisel3._
import chisel3.util._
import hajime.common._

class CSRFileReadReq() extends Bundle {
  val csr = UInt(12.W)
}

class CSRFileReadResp(implicit params: HajimeCoreParams) extends Bundle {
  val data = UInt(params.xprlen.W)
}

class CSRFileWriteReq(implicit params: HajimeCoreParams) extends Bundle {
  val csr = UInt(12.W)
  val data = UInt(params.xprlen.W)
}

class CSRFileIO(implicit params: HajimeCoreParams) extends Bundle {
  val readReq = Input(new CSRFileReadReq())
  val readResp = Output(new CSRFileReadResp())
  val writeReq = Flipped(ValidIO(new CSRFileWriteReq()))
  val cpu_operating = Input(Bool())
  val inst_retire = Input(Bool())
  val hartid = Input(UInt(params.xprlen.W))
}

class CSRFile(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new CSRFileIO())

  def zeroInit_Register(): UInt = {
    RegInit(0.U(params.xprlen.W))
  }

  val cycle = zeroInit_Register()
  val time_counter = RegInit(0.U(log2Up(params.frequency).W))
  when(io.cpu_operating) {
    cycle := cycle + 1.U(params.xprlen.W)
    time_counter := time_counter + 1.U(time_counter.getWidth.W)
  }
  val time = zeroInit_Register()
  when(time_counter === params.frequency.U) {
    time := time + 1.U(params.xprlen.W)
    time_counter := 0.U
  }
  val instret = zeroInit_Register()
  when(io.inst_retire) {
    instret := instret + 1.U(params.xprlen.W)
  }
  val mvendorid = "h426F79734C6F7665".U(params.xprlen.W)
  val marchid = "h53686F7461636F6E".U(params.xprlen.W)
  val mimpid = "h0001145141919810".U(params.xprlen.W)
  val mhartid = io.hartid
  val mstatus = zeroInit_Register()
  val misa = zeroInit_Register()
  val medeleg = zeroInit_Register()
  val mideleg = zeroInit_Register()
  val mie = zeroInit_Register()
  val mtvec = zeroInit_Register()
  val mcounteren = zeroInit_Register()
  val mstatush = zeroInit_Register()
  val mscratch = zeroInit_Register()
  val mepc = zeroInit_Register()
  val mcause = zeroInit_Register()
  val mtval = zeroInit_Register()
  val mip = zeroInit_Register()
  val mtinst = zeroInit_Register()
  val mtval2 = zeroInit_Register()

  io.readResp.data := MuxLookup(io.readReq.csr, 0.U(params.xprlen.W))(Seq(
    "hC00".U(12.W) -> cycle,
    "hC01".U(12.W) -> time,
    "hC02".U(12.W) -> instret,
    "hF11".U(12.W) -> mvendorid,
    "hF12".U(12.W) -> marchid,
    "hF13".U(12.W) -> mimpid,
    "hF14".U(12.W) -> mhartid,
    "h300".U(12.W) -> mstatus,
  ))
}
