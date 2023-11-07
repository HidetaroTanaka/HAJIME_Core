package hajime.vectorOoO

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common.BundleInitializer._
import hajime.common._
import hajime.publicmodules._
import hajime.simple4Stage._
import hajime.vectormodules._

class RenameReq extends Bundle {
  val num = UInt(5.W)
  val useAs = UseRegisterAs()
}

class RetiredReg(implicit params: HajimeCoreParams) extends Bundle {
  val num = UInt(5.W)
  val renamedNum = UInt(params.physicalRegWidth.W)
  val useAs = UseRegisterAs()
}

class RenameReqIO(implicit params: HajimeCoreParams) extends Bundle {
  val rs1 = Input(new RenameReq())
  val rs2 = Input(new RenameReq())
  val rs3 = Input(new RenameReq())
  val rd = Input(new RenameReq())
  // rdやvdへ書き込むならばフリーリストからの割当を行う
  val writeToRd = Input(Bool())
  val stall = Output(Bool())

  val renamedRs1 = Output(new RenamedReg())
  val renamedRs2 = Output(new RenamedReg())
  val renamedRs3 = Output(new RenamedReg())
  val renamedRd = Output(new RenamedReg())
  val retiredRd = Input(Valid(new RetiredReg()))
  val bpMiss = Input(Bool())
}

class RenameUnit(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new RenameReqIO())

  // 投機状態のマップテーブル
  val speculativeScalarRegMapTable = RegInit(VecInit(
    (0 until 32).map(_.U(params.physicalRegWidth.W))
  ))
  val speculativeVectorRegMapTable = RegInit(VecInit(
    (0 until 32).map(_.U(params.physicalRegWidth.W))
  ))

  // リタイア状態のマップテーブル
  val retireScalarRegMapTable = RegInit(VecInit(
    (0 until 32).map(_.U(params.physicalRegWidth.W))
  ))
  val retireVectorRegMapTable = RegInit(VecInit(
    (0 until 32).map(_.U(params.physicalRegWidth.W))
  ))

  // フリーリスト
  // 初期値: 32, 33, ..., 47
  val scalarFreeList = RegInit(VecInit(
    (0 until params.physicalRegFileEntriesFor1Thread).map(
      i => (if(0 <= i && i < params.physicalRegFileEntriesFor1Thread-32) i+32 else 0).U(params.physicalRegWidth.W)
    )
  ))
  val scalarFreeListHead = RegInit(0.U(params.physicalRegWidth.W))
  val scalarFreeListTail = RegInit((params.physicalRegFileEntriesFor1Thread-32).U(params.physicalRegWidth.W))

  val vectorFreeList = RegInit(VecInit(
    (0 until params.physicalRegFileEntriesFor1Thread).map(
      i => (if (0 <= i && i < params.physicalRegFileEntriesFor1Thread - 32) i + 32 else 0).U(params.physicalRegWidth.W)
    )
  ))
  val vectorFreeListHead = RegInit(0.U(params.physicalRegWidth.W))
  val vectorFreeListTail = RegInit((params.physicalRegFileEntriesFor1Thread - 32).U(params.physicalRegWidth.W))
  def freeListPtrNext(ptr: UInt): UInt = Mux(ptr +& 1.U === params.physicalRegFileEntriesFor1Thread.U, 0.U, ptr +& 1.U)
  // headとtailが同じならばempty
  val scalarFreeListEmpty = (scalarFreeListHead === scalarFreeListTail)

  io.stall := scalarFreeListEmpty
  def appendToFreeList(freeList: Vec[UInt], tail: UInt, data: UInt): Unit = {
    freeList(tail) := data
    tail := freeListPtrNext(tail)
  }
  def readFromFreeList(freeList: Vec[UInt], head: UInt): UInt = {
    val data = freeList(head)
    head := freeListPtrNext(head)
    data
  }

  // 分岐予測ミスで最後のリタイアからやり直す場合，リタイア状態のマップテーブルを投機状態に入れる
  // jalrの分岐予測ミスでrdが非零の場合，rdの更新を反映させる
  when(io.bpMiss) {
    for(((speculative, retire), i) <- (speculativeScalarRegMapTable zip retireScalarRegMapTable).zipWithIndex) {
      speculative := Mux(io.retiredRd.valid && (i.U === io.retiredRd.bits.num), io.retiredRd.bits.renamedNum, retire)
    }
    for((speculative, retire) <- (speculativeVectorRegMapTable zip retireVectorRegMapTable)) {
      speculative := retire
    }
  }

  // 読み出すレジスタのリネーミングは投機状態を見れば良い
  io.renamedRs1.useAs := io.rs1.useAs
  io.renamedRs1.num := MuxLookup(io.rs1.useAs, speculativeScalarRegMapTable(io.rs1.num))(Seq(
    UseRegisterAs.SCALAR -> speculativeScalarRegMapTable(io.rs1.num),
    UseRegisterAs.VECTOR -> speculativeVectorRegMapTable(io.rs1.num),
  ))
  io.renamedRs2.useAs := io.rs2.useAs
  io.renamedRs2.num := MuxLookup(io.rs2.useAs, speculativeScalarRegMapTable(io.rs2.num))(Seq(
    UseRegisterAs.SCALAR -> speculativeScalarRegMapTable(io.rs2.num),
    UseRegisterAs.VECTOR -> speculativeVectorRegMapTable(io.rs2.num),
  ))
  io.renamedRs3.useAs := io.rs3.useAs
  io.renamedRs3.num := speculativeVectorRegMapTable(io.rs3.num)

  // 書き込むレジスタのリネーミングはfreeListから取り出し，投機状態マップテーブルを更新
  when(io.writeToRd) {
    assert(io.rd.useAs =/= UseRegisterAs.NONE, "specify rd usage when write to rd")
    io.renamedRd.useAs := io.rd.useAs
    io.renamedRd.num := 0.U
    when(io.rd.useAs === UseRegisterAs.SCALAR) {
      io.renamedRd.num := readFromFreeList(scalarFreeList, scalarFreeListHead)
      speculativeScalarRegMapTable(io.rd.num) := io.renamedRd.num
    } .elsewhen(io.rd.useAs === UseRegisterAs.VECTOR) {
      io.renamedRd.num := readFromFreeList(vectorFreeList, vectorFreeListHead)
      speculativeVectorRegMapTable(io.rd.num) := io.renamedRd.num
    }
  } .otherwise {
    io.renamedRd.useAs := UseRegisterAs.NONE
    io.renamedRd.num := 0.U
  }

  // リタイアする場合はfreeListに使用済みのものを入れ，リタイア状態マップテーブルを更新
  when(io.retiredRd.valid) {
    switch(io.retiredRd.bits.useAs) {
      is(UseRegisterAs.SCALAR) {
        appendToFreeList(scalarFreeList, scalarFreeListTail, retireScalarRegMapTable(io.retiredRd.bits.num))
        retireScalarRegMapTable(io.retiredRd.bits.num) := io.retiredRd.bits.renamedNum
      }
      is(UseRegisterAs.VECTOR) {
        appendToFreeList(vectorFreeList, vectorFreeListTail, retireVectorRegMapTable(io.retiredRd.bits.num))
        retireVectorRegMapTable(io.retiredRd.bits.num) := io.retiredRd.bits.renamedNum
      }
    }
  }
}

object RenameUnit extends App {
  implicit val params = HajimeCoreParams()
  ChiselStage.emitSystemVerilogFile(new RenameUnit(), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}