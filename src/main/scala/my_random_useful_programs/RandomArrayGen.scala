package my_random_useful_programs

import scala.util.Random

object RandomArrayGen extends App {
  val randomArray = Random.shuffle((0 until 32).toList)
  println(randomArray.mkString("{", ", ", "}"))
}
