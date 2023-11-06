package hajime.common

import chisel3._
import chisel3.experimental._
object BundleInitializer {
  implicit class AddBundleInitializerConstructor[T <: Record](x: T) {
    def Init(elems: (T => (Data, Data))*)(implicit sourceInfo: SourceInfo): T = {
      val w = Wire(x)
      for(e <- elems) {
        e(w)._1 := e(w)._2
      }
      w
    }
  }
}
