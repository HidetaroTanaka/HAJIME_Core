package hajime.common

import chisel3._
import chisel3.util._
import Functions._

import scala.annotation.unused

class InstBundle(implicit params: HajimeCoreParams) extends Bundle {
  import params._
  val bits: UInt = UInt(32.W)

  def funct7: UInt    = bits(31,25)
  def rs2: UInt       = bits(24,20)
  def rs1: UInt       = bits(19,15)
  def funct3: UInt    = bits(14,12)
  def rd: UInt        = bits(11,7)
  def opcode: UInt    = bits(6,0)
  def getImm(immType: ImmediateEnum.Type): UInt = {
    MuxLookup(immType, 0.U)(Seq(
      ImmediateEnum.I -> bits(31,20).ext(xprlen),
      ImmediateEnum.S -> Cat(this.funct7, this.rd).ext(xprlen),
      ImmediateEnum.B -> Cat(bits(31), bits(7), bits(30, 25), bits(11,8), 0.U(1.W)).ext(xprlen),
      ImmediateEnum.U -> Cat(bits(31,12), 0.U(12.W)).ext(xprlen),
      ImmediateEnum.J -> Cat(bits(31), bits(19,12), bits(20), bits(30,21), 0.U(1.W)).ext(xprlen)
    ))
  }
  def zimm: UInt      = bits(31,20)
  def uimm19To15: UInt      = Cat(false.B, this.rs1)
  def imm19To15: UInt = this.rs1.ext(xprlen)
}

class ProgramCounter(implicit params: HajimeCoreParams) extends Bundle {
  val addr = UInt(params.xprlen.W)
  def nextPC: UInt = addr + 4.U(params.xprlen.W)

  // C拡張用
  @unused
  def nextCompressedPC: UInt = addr + 2.U(params.xprlen.W)
  def initialise(initial: UInt): ProgramCounter = {
    val initalisedWire = Wire(this)
    initalisedWire.addr := initial
    initalisedWire
  }
}
