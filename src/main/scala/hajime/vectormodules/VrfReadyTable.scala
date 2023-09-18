package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common._

class VecRegIdxWithVtype(implicit params: HajimeCoreParams) extends Bundle {
  val idx = UInt(5.W)
  val vtype = new VtypeBundle()
  val vm = Bool()
}

// TODO: ベクタ実行ユニットの数だけ各IOを用意 Vec(vrfPortNum, ...)
class VrfReadyTableIO(implicit params: HajimeCoreParams) extends Bundle {
  val invalidateIdx = Flipped(ValidIO(new VecRegIdxWithVtype()))
  val validateIdx = Flipped(ValidIO(new VecRegIdxWithVtype()))
  val vs1Check = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vs2Check = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vdCheck = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vmCheck = Flipped(DecoupledIO())
}

class VrfReadyTable(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new VrfReadyTableIO())
  /**
   * IDステージにvdに書き込む命令があった場合，次のクロックで該当vdがfalseになる
   *
   * vdの全演算が終了したらtrueになる
   *
   * vdがfalseであり，かつvdへの有効な書き込みが読み込みのインデックス位置より小さければ発行可能
   *
   * 例：vmsltの7要素目への書き込みが完了すれば，vaddの0要素目(7~0ビット)が実行可能
   * vmsltの8~14要素目への書き込みであれば，vaddの1要素目(15~8ビット)はストール
   */
  val vrfReadyTable = RegInit(VecInit((0 until 32).map(_ => true.B)))

  when(io.invalidateIdx.valid) {
    vrfReadyTable(io.invalidateIdx.bits.idx) := false.B
  }

  /** TODO: validな入力が無い場合でも，過去の入力から正しい値が得られればreadyをtrueにする．
   * 例：
   * vmslt 0  1  2  3  4  5  6  7  8  9 10 11 12 13 ...
   * vadd              0           1           2    ...
   * vse                     0           1          ...
   *                      |
   *                      ここでvalidateIdxのidxが1なので，0に有効な値があるのでready := trueである
   */
  when(io.validateIdx.valid) {
    vrfReadyTable(io.validateIdx.bits.idx) := true.B
    // bypass logic
    io.vs1Check.ready := Mux(io.vs1Check.bits === io.validateIdx.bits, true.B, io.vs1Check.valid && vrfReadyTable(io.vs1Check.bits.idx))
  } otherwise {
    io.vs1Check.ready := io.vs1Check.valid && vrfReadyTable(io.vs1Check.bits.idx)
    io.vs2Check.ready := io.vs2Check.valid && vrfReadyTable(io.vs2Check.bits.idx)
    io.vdCheck.ready := io.vdCheck.valid && vrfReadyTable(io.vdCheck.bits.idx)
    io.vmCheck.ready := io.vmCheck.valid && vrfReadyTable(0)
  }
}
