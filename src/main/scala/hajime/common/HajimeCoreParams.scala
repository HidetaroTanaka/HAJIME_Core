package hajime.common

import chisel3._
import chisel3.util._

// TODO: add inst/data memory info
// Do I even need multi issue? Vector and Multiply can overlap
case class HajimeCoreParams(
  threads: Int = 1,
  xprlen: Int = 64,
  frequency: Int = 50*1000*1000, // x[MHz] = x * 1000 * 1000
  physicalRegFileEntriesFor1Thread: Int = 48,
  ras_depth: Int = 8,
  robEntries: Int = 8,
  useException: Boolean = true,
  useAtomics: Boolean = false,
  useCompressed: Boolean = false,
  useZicsr: Boolean = true,
  useMulDiv: Boolean = true, // Umm actually, this core only supports multiplication
  useFloat32: Boolean = false,
  useFloat64: Boolean = false,
  useFloat128: Boolean = false,
  useAtomic: Boolean = false,
  useBitManip: Boolean = false,
  useRV32E: Boolean = false,
  useHypervisor: Boolean = false,
  useSupervisor: Boolean = false,
  useUser: Boolean = false,
  useDynamicTransLang: Boolean = false,
  useUserLevelInt: Boolean = false,
  useVector: Boolean = true,
  usePackedSIMD: Boolean = false,
  debug: Boolean = true,
  fpga: Boolean = false,
  vlen: Int = 256,
  vecAluExecUnitNum: Int = 2,
) {
  def physicalRegWidth: Int = log2Up(physicalRegFileEntriesFor1Thread)
  def robTagWidth: Int = log2Up(robEntries)
  def generateDefaultMISA: UInt = {
    Cat((xprlen match {
      case 32 => "b01".U(2.W)
      case 64 => "b10".U(2.W)
      case 128 => "b11".U(2.W)
      case _ => throw new Exception(s"XLEN ${xprlen} is invalid.")
    }), 0.U((xprlen - 28).W),
      Cat(Seq(
        useAtomic, // A
        useBitManip, // B
        useCompressed, // C
        useFloat64, // D
        useRV32E, // E
        useFloat32, // F
        false, // G
        useHypervisor, // H
        !useRV32E, // I
        useDynamicTransLang, // J
        false, // K
        false, // L
        useMulDiv, // M
        useUserLevelInt, // N
        false, // O
        usePackedSIMD, // P
        useFloat128, // Q
        false, // R
        useSupervisor, // S
        false, // T
        useUser, // U
        useVector, // V
        false, // W
        false, // X
        false, // Y
        false, // Z
      ).map(_.B).reverse
      )
    )
  }
  def vlenb: Int = vlen/8
}
