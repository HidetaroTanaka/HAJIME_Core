package hajime.common

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.axiIO._

class CacheIO_to_AXI4Lite(addr_width: Int, data_width: Int) extends Module {
  val io = IO(new Bundle{
    val cacheIO = new CacheIO(addr_width, data_width)
    val axi4LiteIO = new AXI4liteIO(addr_width, data_width)
  })

  val _state_input :: _state_reg_read :: _state_reg_write :: Nil = Enum(3)

  val state_reg = RegInit(_state_input)

  val current_state = MuxCase(state_reg, Seq(
    ((state_reg === _state_input) && !io.axi4LiteIO.r.valid && RegNext(io.cacheIO.req.valid && io.cacheIO.req.bits.read && io.axi4LiteIO.ar.ready)) -> _state_reg_read,
    ((state_reg === _state_input) && !io.axi4LiteIO.b.valid && RegNext(
      io.cacheIO.req.valid && io.cacheIO.req.bits.write && io.axi4LiteIO.aw.ready && io.axi4LiteIO.w.ready
    )) -> _state_reg_write,
    ((state_reg === _state_reg_read) && io.axi4LiteIO.r.valid) -> _state_input,
    ((state_reg === _state_reg_write) && io.axi4LiteIO.b.valid) -> _state_input,
  ))
  state_reg := current_state

  // cacheIOからのreqで、次のクロックで保持
  // 次のクロックでreadの場合にAXI4LiteのRがvalidにならない、またはwriteの場合にBがvalidにならない場合は更新しない
  val cacheIO_currentReq_reg = Reg(new CacheReq(addr_width, data_width))
  when(io.cacheIO.req.valid && (current_state === _state_input)) {
    cacheIO_currentReq_reg := io.cacheIO.req.bits
  }

  io.cacheIO := DontCare
  io.axi4LiteIO := DontCare

  when(current_state === _state_input) {
    when(io.cacheIO.req.valid && io.cacheIO.req.bits.read) {
      io.axi4LiteIO.ar.valid := true.B
      io.axi4LiteIO.ar.bits.addr := io.cacheIO.req.bits.addr
      io.axi4LiteIO.ar.bits.prot := 0.U
      io.axi4LiteIO.aw.valid := false.B
      io.axi4LiteIO.aw.bits.addr := 0.U
      io.axi4LiteIO.aw.bits.prot := 0.U
      io.axi4LiteIO.b.ready := false.B
      io.axi4LiteIO.r.ready := true.B
      io.axi4LiteIO.w.valid := false.B
      io.axi4LiteIO.w.bits.data := 0.U
      io.axi4LiteIO.w.bits.strb := 0.U
    } .elsewhen(io.cacheIO.req.valid && io.cacheIO.req.bits.write) {
      io.axi4LiteIO.ar.valid := false.B
      io.axi4LiteIO.ar.bits.addr := 0.U
      io.axi4LiteIO.ar.bits.prot := 0.U
      io.axi4LiteIO.aw.valid := true.B
      io.axi4LiteIO.aw.bits.addr := io.cacheIO.req.bits.addr
      io.axi4LiteIO.aw.bits.prot := 0.U
      io.axi4LiteIO.b.ready := true.B
      io.axi4LiteIO.r.ready := false.B
      io.axi4LiteIO.w.valid := true.B
      io.axi4LiteIO.w.bits.data := io.cacheIO.req.bits.data
      io.axi4LiteIO.w.bits.strb := MuxLookup(io.cacheIO.req.bits.function, 0.U)(Seq(
        CACHE_FUNCTIONS.BYTE -> "b01".U,
        CACHE_FUNCTIONS.HALFWORD -> "b011".U,
        CACHE_FUNCTIONS.WORD -> "b01111".U,
        CACHE_FUNCTIONS.DOUBLEWORD -> "b11111111".U
      ))
    }
  } .elsewhen(current_state === _state_reg_read) {
    io.axi4LiteIO.ar.valid := true.B
    io.axi4LiteIO.ar.bits.addr := cacheIO_currentReq_reg.addr
    io.axi4LiteIO.ar.bits.prot := 0.U
    io.axi4LiteIO.aw.valid := false.B
    io.axi4LiteIO.aw.bits.addr := 0.U
    io.axi4LiteIO.aw.bits.prot := 0.U
    io.axi4LiteIO.b.ready := false.B
    io.axi4LiteIO.r.ready := true.B
    io.axi4LiteIO.w.valid := false.B
    io.axi4LiteIO.w.bits.data := 0.U
    io.axi4LiteIO.w.bits.strb := 0.U
  } .elsewhen(current_state === _state_reg_write) {
    io.axi4LiteIO.ar.valid := false.B
    io.axi4LiteIO.ar.bits.addr := 0.U
    io.axi4LiteIO.ar.bits.prot := 0.U
    io.axi4LiteIO.aw.valid := true.B
    io.axi4LiteIO.aw.bits.addr := cacheIO_currentReq_reg.addr
    io.axi4LiteIO.aw.bits.prot := 0.U
    io.axi4LiteIO.b.ready := true.B
    io.axi4LiteIO.r.ready := false.B
    io.axi4LiteIO.w.valid := true.B
    io.axi4LiteIO.w.bits.data := cacheIO_currentReq_reg.data
    io.axi4LiteIO.w.bits.strb := MuxLookup(cacheIO_currentReq_reg.function, 0.U)(Seq(
      CACHE_FUNCTIONS.BYTE -> "b01".U,
      CACHE_FUNCTIONS.HALFWORD -> "b011".U,
      CACHE_FUNCTIONS.WORD -> "b01111".U,
      CACHE_FUNCTIONS.DOUBLEWORD -> "b11111111".U
    ))
  }

  when(current_state === _state_input) {
    io.cacheIO.req.ready := io.axi4LiteIO.ar.ready && io.axi4LiteIO.aw.ready && io.axi4LiteIO.w.ready
    io.cacheIO.resp.valid := MuxCase(false.B, Seq(
      cacheIO_currentReq_reg.read -> io.axi4LiteIO.r.valid,
      cacheIO_currentReq_reg.write -> io.axi4LiteIO.w.valid,
    ))
    io.cacheIO.resp.bits.data := io.axi4LiteIO.r.bits.data
  }
}

object CacheIO_to_AXI4Lite extends App {
  (new ChiselStage).emitSystemVerilog(new CacheIO_to_AXI4Lite(addr_width = 64, data_width = 64), args = COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
