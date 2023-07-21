package hajime.common

import chisel3._
import chisel3.util._

case class HajimeCoreParams(
  issue_width: Int = 2,
  xprlen: Int = 64,
  physicalRegFileEntries: Int = 48,
  robEntries: Int = 8,
  useAtomics: Boolean = false,
  useCompressed: Boolean = false,
  useZicsr: Boolean = false,
  useMulDiv: Boolean = false,
  useFloat32: Boolean = false,
  useFloat64: Boolean = false,
  useVector: Boolean = false,
  usePackedSIMD: Boolean = false,
  debug: Boolean = true,
) {
  def robTagWidth: Int = log2Up(robEntries)
}
