package service

import morphir.example.app.ScumAndVillainy.Rolls

object Roller {
  def randomNumbers(n: Int): Rolls = {
    val rand = new scala.util.Random
    (1 to 1.max(n)).map(_ => rand.nextInt(6) + 1).toList
  }
}
