package my_random_useful_programs

import scala.io._

object Data32To64 extends App {
  val fileLines = Source.fromFile("src/main/resources/applications_vector/vector_matmul_data.hex").getLines().toSeq
  val ls = fileLines ++ (if(fileLines.length % 2 != 0) Seq("00000000") else Nil)
  val strList = for(i <- 0 until ls.length/2) yield {
    ls(i*2+1) + ls(i*2)
  }
  strList.foreach(println)
}
