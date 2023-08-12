package hajime.publicmodules
import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import chisel3.experimental.BundleLiterals._
import hajime.axiIO._
import hajime.common.COMPILE_CONSTANTS
import chiseltest._
import org.scalatest.flatspec._

import scala.io._

// 命令キャッシュと異なりマスター側のreadyが下がることは無いので，出力のストールは考えない
class Dcache_for_Verilator(dcacheBaseAddr: Int, tohost: Int, memsize: Int = 0x2000) extends Module with ChecksAxiReadResp with ChecksAxiWriteResp{
  require(memsize % 8 == 0, s"memsize $memsize is not multiple of 8")

  val io = IO(Flipped(new AXI4liteIO(addr_width = 64, data_width = 64)))
  val debug = IO(ValidIO(UInt(32.W)))

  val debugToHost = RegInit(WireInit(Valid(UInt(32.W)).Lit(
    _.bits -> 0.U,
    _.valid -> false.B,
  )))
  debug := debugToHost
  debugToHost.valid := false.B

  io.aw.ready := true.B
  io.ar.ready := true.B
  io.w.ready := true.B

  val internalReadAddr = io.ar.bits.addr - dcacheBaseAddr.U
  val internalWriteAddr = io.aw.bits.addr - dcacheBaseAddr.U

  // 8Byte * 4096 -> 32768 Byte
  // -> 0x8000 -> 0x1000 (Vecが8要素だから？)
  // -> 0x00004000 ~ 0x00004FFF
  val mem = SyncReadMem(memsize/2, Vec(8, UInt(8.W)))

  // read
  val readDataFromMem = Wire(UInt(64.W))
  readDataFromMem := Cat(mem.read(internalReadAddr.head(61)).reverse)
  // Byte: 0x1FFF以下
  // HalfWord: 0x1FFE以下，addr+1が8Byteを超えない
  // Word: 0x1FFC以下，addr+3が8Byteを超えない
  // DoubleWord: 0x1FF8以下，整列のみ
  // メモリロードの際にわからないので，全てコアに丸投げすることとする
  io.r.bits.resp := R_OKEY.U

  io.r.bits.data := MuxLookup(RegNext(internalReadAddr(2,0)), readDataFromMem)(
    (0 until 8).map(
      i => i.U -> readDataFromMem(63, i*8)
    )
  )
  io.r.valid := RegNext(io.ar.valid && io.ar.ready)

  // write
  val writeData_asVec = Wire(Vec(8, UInt(8.W)))
  val shiftedData = MuxLookup(internalWriteAddr(2,0), io.w.bits.data)(
    (0 until 8).map(
      i => i.U -> (io.w.bits.data << (i*8).U).asUInt
    )
  )
  for((w,i) <- writeData_asVec.zipWithIndex) {
    w := shiftedData(8*i+7, 8*i)
  }
  when(io.aw.valid && io.w.valid && internalWriteAddr < 0x00001FFF.U) {
    mem.write(internalWriteAddr.head(61), writeData_asVec, MuxLookup(internalWriteAddr(2,0), io.w.bits.strb)(
      (0 until 8).map(
        i => i.U -> (io.w.bits.strb << i.U).asUInt(7,0)
      )
    ).asBools)
  } .elsewhen(io.aw.valid && io.w.valid && io.aw.bits.addr === tohost.U) {
    debugToHost.bits := io.w.bits.data
    debugToHost.valid := true.B
  }
  io.b.valid := RegNext(io.aw.valid && io.w.valid)
  io.b.bits.resp := W_OKEY.U
}

object Dcache_for_Verilator extends App {
  def apply(dcacheBaseAddr: Int = 0x00004000, tohost: Int = 0x10000000, memsize: Int = 0x2000): Dcache_for_Verilator = new Dcache_for_Verilator(dcacheBaseAddr, tohost, memsize)
  ChiselStage.emitSystemVerilogFile(Dcache_for_Verilator(), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}