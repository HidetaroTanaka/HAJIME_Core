import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import hajime.axiIO.AXI4liteIO


/**
 * Read Only AXI4-Lite Memory because thinking about writing is pain
 */
class Icache_model(hexfileName: String) extends Module {
  val io = IO(Flipped(new AXI4liteIO(addr_width = 64, data_width = 32)))

  val AR_HAS_ACCESS_DELAY = false
  val R_HAS_ACCESS_DELAY = false

  io := DontCare

  val ar_addr_reg = Reg(Valid(chiselTypeOf(io.ar.bits.addr)))
  when(io.ar.valid && io.ar.ready) {
    ar_addr_reg.bits := io.ar.bits.addr
    ar_addr_reg.valid := io.ar.valid
  } .otherwise {
    ar_addr_reg.valid := false.B
  }

  val mem = SyncReadMem(4096, UInt(8.W))
  if(hexfileName != null) loadMemoryFromFile(mem, hexfileName)
  // loadMemoryFromFile(mem, "src/main/resources/addi_dump_inst.hex")

  val readData_vec = Wire(Vec(4, UInt(8.W)))
  readData_vec.foreach(_ := 0.U)

  val addr_to_mem = Mux(io.ar.ready, io.ar.bits.addr, ar_addr_reg.bits)
  for((d,i) <- readData_vec.zipWithIndex) {
    d := mem.read(addr_to_mem + i.U)
  }

  val readData_vec_reg = Reg(chiselTypeOf(readData_vec))

  when(!io.r.ready) {
    readData_vec_reg := readData_vec_reg
  } .otherwise {
    readData_vec_reg := readData_vec
  }

  val internal_hash0 = (io.ar.bits.addr(5,4) ^ io.ar.bits.addr(3,2)).andR
  val internal_hash1 = ar_addr_reg.bits(5,4).andR ^ ar_addr_reg.bits(3,2).andR
  val hash0_reg = RegNext(internal_hash0)
  val hash1_reg = RegNext(internal_hash1)
  io.ar.ready := !ar_addr_reg.valid || (io.r.valid && io.r.ready) && (
    if(AR_HAS_ACCESS_DELAY) {
      !internal_hash0 || (internal_hash0 && hash0_reg)
    }
    else {
      true.B
    }
    )

  io.r.bits.data := Cat(readData_vec.reverse)
  io.r.bits.resp := 0.U(3.W)
  io.r.valid := {
    RegNext(io.ar.valid && io.ar.ready) && (if (R_HAS_ACCESS_DELAY) {
      !internal_hash1 || (internal_hash1 && hash1_reg)
    } else {
      true.B
    })
  }
}

object Icache_model extends App {
  def apply(hexfileName: String): Icache_model = new Icache_model(hexfileName)
  (new ChiselStage).emitVerilog(this.apply(null), args = hajime.common.COMPILE_CONSTANTS.CHISELSTAGE_ARGS)
}
