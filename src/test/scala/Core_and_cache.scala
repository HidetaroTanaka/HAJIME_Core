import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import hajime.common.RISCV_Consts
import hajime.simple4Stage._

class Core_and_cache extends Module {
  val io = IO(new Bundle{
    val reset_vector = Input(UInt(64.W))
    val debug = Output(UInt(64.W))
    val debug_retired_inst = Output(Valid(UInt(32.W)))
    val debug_abi_map = Output(new debug_map_physical_to_abi(64))
  })

  val core = Module(Core(xprlen = 64, debug = true))
  val icache = Module(new Icache_model)
  val dcache = Module(new Dcache_model(dcacheBaseAddr = 0x00004000, tohost = 0x00001000))

  core.io.icache_axi4lite <> icache.io
  core.io.dcache_axi4lite <> dcache.io

  core.io.reset_vector := io.reset_vector
  io.debug := dcache.debug
  io.debug_retired_inst := core.io.debug_retired_inst.get
  io.debug_abi_map := core.io.debug_abi_map.get
}

object Core_and_cache extends App {
  def apply: Core_and_cache = new Core_and_cache
  (new ChiselStage).emitVerilog(apply, args = hajime.common.COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
