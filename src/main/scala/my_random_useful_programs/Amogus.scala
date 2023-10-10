package my_random_useful_programs

object Amogus extends App {
  val instList = List("vminu", "vmin", "vmaxu", "vmax")
  val vsource = List("VV", "VX")
  instList.foreach(
    x => vsource.foreach(
      y => println(s"  def ${x.toUpperCase}_$y = vArithGen(vInst = \"$x\", vsource = VSOURCE.$y)")
    )
  )
}
