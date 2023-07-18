package my_random_useful_programs

object Int32ToHex_Generator extends App {
  for(i <- 0 until 16) {
    val hex = i.toHexString.toUpperCase
    println(s"      case 0x${hex}:")
    println(s"        str[9-i] = '${hex}';")
    println( "        break;")
  }
}
