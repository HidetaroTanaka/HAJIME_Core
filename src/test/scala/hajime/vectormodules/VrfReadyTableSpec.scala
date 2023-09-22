package hajime.vectormodules

import chisel3._
import chiseltest._
import hajime.common.HajimeCoreParams
import org.scalatest.flatspec._
import chisel3.experimental.BundleLiterals._

class VrfReadyTableSpec extends AnyFlatSpec with ChiselScalatestTester {
  def invalidate(dut: VrfReadyTable, instVd: UInt, instVsew: UInt, instVm: Bool, veuNum: Int, resultInNextClock: Boolean)(implicit params: HajimeCoreParams): Unit = {
    dut.io.invalidateIdx.valid.poke(true.B)
    dut.io.invalidateIdx.bits.idx.poke(instVd)
    dut.io.invalidateIdx.bits.vtype.poke(new VtypeBundle().Lit(
      _.vill -> false.B,
      _.vma -> false.B,
      _.vta -> false.B,
      _.vsew -> instVsew,
      _.vlmul -> 0.U
    ))
    dut.io.invalidateIdx.bits.vm.poke(instVm)
    dut.io.vs1Check.valid.poke(false.B)
    dut.clock.step()
    dut.io.fromVecExecUnit(veuNum).valid.poke(true.B)
    dut.io.fromVecExecUnit(veuNum).bits.vd.poke(instVd)
    dut.io.fromVecExecUnit(veuNum).bits.vtype.poke(new VtypeBundle().Lit(
      _.vill -> false.B,
      _.vma -> false.B,
      _.vta -> false.B,
      _.vsew -> instVsew,
      _.vlmul -> 0.U
    ))
    dut.io.fromVecExecUnit(veuNum).bits.vm.poke(instVm)
    dut.io.fromVecExecUnit(veuNum).bits.writeReq.poke(resultInNextClock.B)
  }
  def checkVs1Ready(dut: VrfReadyTable, instVs1: UInt, instVsew: UInt, instVm: Bool, expect: Bool)(implicit params: HajimeCoreParams): Unit = {
    dut.io.vs1Check.valid.poke(true.B)
    dut.io.vs1Check.bits.idx.poke(instVs1)
    dut.io.vs1Check.bits.vtype.poke(new VtypeBundle().Lit(
      _.vill -> false.B,
      _.vma -> false.B,
      _.vta -> false.B,
      _.vsew -> instVsew,
      _.vlmul -> 0.U
    ))
    dut.io.vs1Check.bits.vm.poke(instVm)
    dut.io.vs1Check.ready.expect(expect)
  }
  def checkVdReady(dut: VrfReadyTable, instVd: UInt, instVsew: UInt, instVm: Bool, expect: Bool)(implicit params: HajimeCoreParams): Unit = {
    dut.io.vdCheck.valid.poke(true.B)
    dut.io.vdCheck.bits.idx.poke(instVd)
    dut.io.vdCheck.bits.vtype.poke(new VtypeBundle().Lit(
      _.vill -> false.B,
      _.vma -> false.B,
      _.vta -> false.B,
      _.vsew -> instVsew,
      _.vlmul -> 0.U
    ))
    dut.io.vdCheck.bits.vm.poke(instVm)
    dut.io.vdCheck.ready.expect(expect)
  }
  def checkVmReady(dut: VrfReadyTable, expect: Bool)(implicit params: HajimeCoreParams): Unit = {
    dut.io.vmCheck.valid.poke(true.B)
    dut.io.vmCheck.ready.expect(expect)
  }
  def reset(dut: VrfReadyTable): Unit = {
    dut.clock.step()
    dut.reset.poke(true.B)
    dut.clock.step()
    dut.reset.poke(false.B)
    dut.io.fromVecExecUnit.foreach(_.valid.poke(false.B))
  }
  it should "return ready correctly" in {
    implicit val params: HajimeCoreParams = HajimeCoreParams()
    test(new VrfReadyTable(vrfPortNum = 3)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      reset(dut)
      for(i <- 0 until 32) {
        checkVs1Ready(dut, instVs1 = i.U, instVsew = 0.U, instVm = false.B, expect = true.B)
      }
      // IDからの発行(vd = v12)
      invalidate(dut, 12.U, 0.U, false.B, 1, resultInNextClock = true)
      checkVs1Ready(dut, 12.U, 0.U, false.B, true.B)
      reset(dut)
      // vmslt.vv v0, v1, v2
      // vadd.vv v13, v14, v15, v0.t
      invalidate(dut, instVd = 0.U, instVsew = 0.U, instVm = true.B, veuNum = 1, resultInNextClock = true)
      checkVmReady(dut, expect = true.B)
      reset(dut)
      // vmslt.vv v0, v1, v2
      // vadd.vv v4, v0, v5
      invalidate(dut, instVd = 0.U, instVsew = 0.U, instVm = true.B, veuNum = 1, resultInNextClock = true)
      checkVs1Ready(dut, instVs1 = 0.U, instVsew = 0.U, instVm = false.B, expect = false.B)
      reset(dut)
      // vmslt.vv v0, v1, v2
      // vadd.vv v4, v5, v9
      invalidate(dut, instVd = 0.U, instVsew = 0.U, instVm = true.B, veuNum = 1, resultInNextClock = true)
      checkVs1Ready(dut, instVs1 = 5.U, instVsew = 0.U, instVm = false.B, expect = true.B)
      reset(dut)
      // vmand.mm v0, v6, v7
      // vadd.vv v9, v10, v11, v0.t
      invalidate(dut, instVd = 0.U, instVsew = 0.U, instVm = true.B, veuNum = 2, resultInNextClock = true)
      checkVmReady(dut, expect = true.B)
      reset(dut)
      // vadd.vv v5, v6, v7
      // vmand.mm v1, v5, v2
      invalidate(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, veuNum = 1, resultInNextClock = true)
      checkVs1Ready(dut, instVs1 = 5.U, instVsew = 0.U, instVm = true.B, expect = true.B)
      reset(dut)
      // vmul.vv v5, v6, v7
      // vadd.vv v10, v5, v9
      invalidate(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, veuNum = 1, resultInNextClock = false)
      checkVs1Ready(dut, instVs1 = 5.U, instVsew = 0.U, instVm = false.B, expect = false.B)
      reset(dut)
      // vadd.vv v5, v6, v7 (e8)
      // vse16.v v5, 0(a0)
      invalidate(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, veuNum = 1, resultInNextClock = true)
      checkVdReady(dut, instVd = 5.U, instVsew = 1.U, instVm = false.B, expect = false.B)
      reset(dut)
      // vadd.vv v5, v6, v7 (e8)
      // vse16.v v4, v3, v2
      // 依存が無いため同じvlなら発行可能
      invalidate(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, veuNum = 1, resultInNextClock = true)
      checkVs1Ready(dut, instVs1 = 3.U, instVsew = 1.U, instVm = false.B, expect = true.B)
      reset(dut)
      // vadd.vv v5, v6, v7 (e8)
      // vse16.v v4, v5, v2
      // 依存があるため発行不可
      invalidate(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, veuNum = 1, resultInNextClock = true)
      checkVs1Ready(dut, instVs1 = 5.U, instVsew = 1.U, instVm = false.B, expect = false.B)
      reset(dut)
      // vle8.v v5, v3, v2
      // vadd.vv v5, v6, v7 (e8)
      invalidate(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, veuNum = 1, resultInNextClock = false)
      checkVdReady(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, expect = false.B)
      // vle16.v v5, v3, v2
      // vadd.vv v5, v6, v7 (e8)
      invalidate(dut, instVd = 5.U, instVsew = 1.U, instVm = false.B, veuNum = 1, resultInNextClock = true)
      checkVdReady(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, expect = true.B)
      // vse8.v v5, (a0)
      // vadd.vv v5, v6, v7 (e16)
      invalidate(dut, instVd = 5.U, instVsew = 0.U, instVm = false.B, veuNum = 1, resultInNextClock = true)
      checkVdReady(dut, instVd = 5.U, instVsew = 1.U, instVm = false.B, expect = false.B)
    }
  }
}
