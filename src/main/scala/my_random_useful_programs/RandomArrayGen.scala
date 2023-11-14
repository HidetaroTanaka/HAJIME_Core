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
  val randomArray = (0 until 3).map(
    _ => (0 until 48).map(_ => Random.nextInt(0xFFFF) - 0x8000)
  )
  val randomArray0 = (0 until 48).map(_ => Random.nextInt(0xFFFF) - 0x8000)
  val randomArray1 = (0 until 48).map(_ => Random.nextInt(0xFFFF) - 0x8000)
  val randomArray2 = (0 until 48).map(_ => Random.nextInt(0xFFFF) - 0x8000)
  val boolArray = (0 until 48).map(_ => function())
  val hasEqualArray = randomArray0.lazyZip(randomArray1).lazyZip(boolArray).map {
    case (v1, v2, b) => if(b) v1 else v2
  }
  randomArray.foreach(
    l => println(l.mkString("{", ", ", "};"))
  )
}