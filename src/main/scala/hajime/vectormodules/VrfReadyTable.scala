package hajime.vectormodules

import chisel3._
import circt.stage.ChiselStage
import chisel3.util._
import hajime.common._
import chisel3.experimental.BundleLiterals._

class VecRegIdxWithVtype(implicit params: HajimeCoreParams) extends Bundle {
  val idx = UInt(5.W)
  val vtype = new VtypeBundle()
  val vm = Bool()
}

// TODO: ベクタ実行ユニットの数だけ各IOを用意 Vec(vrfPortNum, ...)
class VrfReadyTableIO(vrfPortNum: Int)(implicit params: HajimeCoreParams) extends Bundle {
  val fromVecExecUnit = Vec(vrfPortNum, Flipped(ValidIO(new VecRegFileWriteReq())))
  // IDステージから発行される命令のvd
  val invalidateIdx = Flipped(ValidIO(new VecRegIdxWithVtype()))
  // IDステージへ各ベクトルレジスタが有効か否かを伝える
  val vs1Check = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vs2Check = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vdCheck = Flipped(DecoupledIO(new VecRegIdxWithVtype()))
  val vmCheck = Flipped(DecoupledIO())
}

class VrfReadyTable(vrfPortNum: Int = 2)(implicit params: HajimeCoreParams) extends Module {
  val io = IO(new VrfReadyTableIO(vrfPortNum))
  /**
   * IDステージにvdに書き込む命令があった場合，次のクロックで該当vdがfalseになる
   * 0要素目への書き込みでtrueになるが，同時に同じベクタレジスタへ書き込む命令が発行されれば後続命令によってfalseにする方を優先
   */
  val vrfZeroIdxReadyTable = RegInit(VecInit((0 until 32).map(_ => true.B)))

  /**
   * IDステージにvdに書き込む命令があった場合，次のクロックで該当vdがfalseになる
   * ベクタレジスタの全要素への書き込みが終了し，かつ他のベクタユニットに同じベクタレジスタに書く命令が格納されていない場合にtrue
   * 同時に同じベクタレジスタへ書き込む命令が発行されるならfalse
   */
  // これいる？
  // val vrfWholeIdxReadyTable = RegInit(VecInit((0 until 32).map(_ => true.B)))

  /**
   * ベクタレジスタの要素幅
   *
   * 0要素目に書き込みがあった場合に，幅の情報を書き込み
   */
  val vrfWriteSewTable = RegInit(VecInit((0 until 32).map(_ => new Bundle {
    val sew = UInt(3.W)
    val vm = Bool()
  }.Lit(
    _.sew -> 0.U(3.W),
    _.vm -> false.B
  ))))

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
  // であれば，依存が無いため発行可能（vlがVLEN/2以下であることが必要，vtypeのvsewと一致しなければillegalにするのもあり）
  // vle8.v v5, v3, v2
  // vadd.vv v5, v6, v7 (e8)
  // であれば，vle8.vがv5.e8(0)に書き込んでいる時にvadd.vvを発行可能
  // vle16.v v5, v3, v2
  // vadd.vv v5, v6, v7 (e8)
  // であれば，v5.e16(0)へ先に書き込まれるため発行可能
  // vse8.v v5, (a0)
  // vadd.vv v5, v6, v7 (e16)
  // であれば，vaddがvseより先にv5へ書き込みWAWハザードのためストール
  for(sigFromVEU <- io.fromVecExecUnit) {
    // vdへの書き込みを行う命令がある場合
    when(sigFromVEU.valid) {
      // 0要素目への書き込みならば
      when(sigFromVEU.bits.writeReq && sigFromVEU.bits.index === 0.U) {
        vrfZeroIdxReadyTable(sigFromVEU.bits.vd) := true.B
        vrfWriteSewTable(sigFromVEU.bits.vd).sew := sigFromVEU.bits.vtype.vsew
        vrfWriteSewTable(sigFromVEU.bits.vd).vm := sigFromVEU.bits.vm
      }
    }
  }
  /*
  // vdレジスタの最後のインデックスへの書き込みが完了し，かつ他のベクトルユニットとvdが被っていなければ，該当vdレジスタの全ての値が利用可能
  for((sigFromVEU, i) <- io.fromVecExecUnit.zipWithIndex) {
    when(sigFromVEU.valid && sigFromVEU.bits.writeReq && sigFromVEU.bits.index === sigFromVEU.bits.vl-1.U) {
      val sigExceptI = io.fromVecExecUnit.patch(i, Nil, 1)
      val otherWritesSameVd = sigExceptI.map(sigs => sigs.valid && sigs.bits.vd === sigFromVEU.bits.vd).reduce(_ || _)
      when(!otherWritesSameVd) {
        vrfWholeIdxReadyTable(sigFromVEU.bits.vd) := true.B
      }
    }
  }
   */

  // IDステージへのバイパス
  // 該当するベクトルレジスタの0要素目がreadyであり，かつ読み込む幅が書き込む幅以下であればtrue
  io.vs1Check.ready := io.vs1Check.valid && vrfZeroIdxReadyTable(io.vs1Check.bits.idx) && (
    io.vs1Check.bits.vm || io.vs1Check.bits.vtype.vsew <= vrfWriteSewTable(io.vs1Check.bits.idx).sew
  )
  io.vs2Check.ready := io.vs2Check.valid && vrfZeroIdxReadyTable(io.vs2Check.bits.idx) && (
    io.vs2Check.bits.vm || io.vs2Check.bits.vtype.vsew <= vrfWriteSewTable(io.vs2Check.bits.idx).sew
  )
  io.vmCheck.ready := io.vmCheck.valid && vrfZeroIdxReadyTable(0)
  // vdも同様
  io.vdCheck.ready := io.vdCheck.valid && vrfZeroIdxReadyTable(io.vdCheck.bits.idx) && (
    io.vdCheck.bits.vm || io.vdCheck.bits.vtype.vsew <= vrfWriteSewTable(io.vdCheck.bits.idx).sew
  )
}

object VrfReadyTable extends App {
  implicit val params = HajimeCoreParams()
  def apply(vrfPortNum: Int)(implicit params: HajimeCoreParams): VrfReadyTable = new VrfReadyTable(vrfPortNum)
  ChiselStage.emitSystemVerilogFile(VrfReadyTable(2), firtoolOpts = COMPILE_CONSTANTS.FIRTOOLOPS)
}
