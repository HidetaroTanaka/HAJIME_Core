package hajime.common

import chisel3._
import chisel3.util._

case class HajimeCoreParams(
  issue_width: Int = 2,
  xprlen: Int = 64,
  physicalRegFileEntries: Int = 48,
  debug: Boolean = true,
)
