package my_random_useful_programs

import scala.util.Random

object RandomArrayGen extends App {
  val randomArray = Random.shuffle((-24 until 23).toList)
  println(randomArray.mkString("{", ", ", "}"))
}

object RandomIntTupleGen extends App {
  val randomTuple = (0 until 16).map(
    _ => {
      val temp0 = Random.nextBytes(1).head.toInt
      val temp1 = Random.nextInt(16)
      (temp0, temp1)
    }
  )
  println(randomTuple.map(_._1).mkString("{", ", ", "}"))
  println(randomTuple.map(_._2).mkString("{", ", ", "}"))
}

object RandomArrayWithElenGen extends App {
  private def function(): Boolean = {
    (0 until 4).map(_ => Random.nextBoolean()).reduce(_ && _)
  }
  val randomArray0 = (0 until 48).map(_ => Random.nextInt(0x100)).map("0x" + _.toHexString.toUpperCase)
  val randomArray1 = (0 until 48).map(_ => Random.nextInt(0x100)).map("0x" + _.toHexString.toUpperCase)
  val boolArray = (0 until 48).map(_ => function())
  val hasEqualArray = randomArray0.lazyZip(randomArray1).lazyZip(boolArray).map {
    case (v1, v2, b) => if(b) v1 else v2
  }
  println(randomArray0.mkString("{", ", ", "};"))
  println(hasEqualArray.mkString("{", ", ", "};"))
}