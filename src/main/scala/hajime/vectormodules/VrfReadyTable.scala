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

class VrfReadyTableToVecExecUnit(implicit params: HajimeCoreParams) extends Bundle {
  val invalidateIdx = Flipped(ValidIO(new VecRegIdxWithVtype()))
  val validateIdx = Flipped(ValidIO(new VecRegFileWriteReq()))
  val vs1Check = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vs2Check = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vdCheck = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vmCheck = Flipped(DecoupledIO())
}

// TODO: ベクタ実行ユニットの数だけ各IOを用意 Vec(vrfPortNum, ...)
class VrfReadyTableIO(vrfPortNum: Int)(implicit params: HajimeCoreParams) extends Bundle {
  val toVecExecUnit = Vec(vrfPortNum, new VrfReadyTableToVecExecUnit())
}

class VrfReadyTable(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new VrfReadyTableIO(2))
  /**
   * IDステージにvdに書き込む命令があった場合，次のクロックで該当vdがfalseになる
   *
   * vdの全演算が終了したらtrueになる
   */
  val vrfReadyTable = RegInit(VecInit((0 until 32).map(_ => true.B)))

  for(sigFromVEU <- io.toVecExecUnit) {
    vrfReadyTable(sigFromVEU.invalidateIdx.bits.idx) := false.B
    when(sigFromVEU.validateIdx.valid) {
      // bypass logic
      // IDに存在するベクタ命令は，全オペランド（ベクタオペランドの0番目の要素，スカラオペランド）が有効かつ，
      // RAWハザードのあるベクタオペランドへの書き込みの幅（SEW，vm）より，読み込みの幅が同じか小さいがベクタ長が同じ（e8 < e16, vm < e8, ...）かつ，
      // ベクタ実行ユニットが利用可能ならば発行可能（このモジュールでは実行ユニットが有効か否かは考えない）
      // 例：
      // vop.** : EXパイプラインに1つ以上存在する命令
      // vop.** : IDパイプラインにある命令
      //
      // vmslt.vv v0, v1, v2
      // vadd.vv v13, v14, v15, v0.t
      // であれば，先行するvmslt命令がv0の0要素目以降にマスク書き込みを行い，後続のvadd.vvがv0からマスク読み出しを行うためチェイニングにより並列実行が可能
      // vmslt.vv v0, v1, v2
      // vadd.vv v4, v5, v0
      // であれば，v0へのマスク書き込みに対し，e8以上の幅での読み出しを行うためvmsltの全ての書き込みが完了するまでストール
      // vmslt.vv v0, v1, v2
      // vadd.vv v4, v5, v9
      // であれば，どちらもvlサイクルなのでチェイニング可能（プログラム順のリタイアが可能）
      // vmand.mm v0, v6, v7
      // vadd.vv v9, v10, v11, v0.t
      // であれば，vmand.mm命令をvlサイクルで終わらせれば上記と同様（仕様ではtail-agnosticだが，動作がundisturbedでも問題なし）
      // vadd.vv v5, v6, v7
      // vmand.mm v1, v2, v5
      // であれば，v5.e8(0)への書き込みでv5.mask(0)が得られるので発行可能
      // vmul.vv v5, v6, v7
      // vadd.vv v10, v5, v9
      // であれば，乗算にはスタートアップ時間が必要なため，v5への書き込みが有効になるまでストール，0要素目への書き込みで発行
      // vadd.vv v5, v6, v7 (e8)
      // vse16.v v5, 0(a0)
      // であれば，v5へのe8書き込みに対し，e16での読み出しが必要なためvaddの全ての書き込みが完了するまでストール
      // vadd.vv v5, v6, v7 (e8)
      // vse16.v v4, v3, v2
      // であれば，依存が無いため発行可能（vlがVLEN/2以下であることが必要）
      // vle8.v v5, v3, v2
      // vadd.vv v5, v6, v7 (e8)
      // であれば，vle8.vがv5.e8(0)に書き込んでいる時にvadd.vvを発行可能
      // vle16.v v5, v3, v2
      // vadd.vv v5, v6, v7 (e8)
      // であれば，
    }
  }

  /**
   * TODO: 過去の入力から正しい値が得られればreadyをtrueにする．
   * 例：
   * vmslt 0  1  2  3  4  5  6  7  8  9 10 11 12 13 ...
   * vadd              0           1           2    ...
   * vse                     0           1          ...
   *                      |
   *                      ここでvalidateIdxのidxが1なので，0に有効な値があるのでready := trueである
   */
  /*
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
   */
}
