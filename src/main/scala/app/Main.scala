package app

import morphir.example.app.ScumAndVillainy.{Outcome, Rolls, SuccessLevel}
import service.Roller
import zhttp.service.Server
import zio.{Random, Scope, ZIO, ZIOAppArgs, ZIOAppDefault}
import zhttp.http._
import zio.json.internal.Write
import zio.json.{DeriveJsonEncoder, EncoderOps, JsonEncoder}

object Main extends ZIOAppDefault {
  override def run: ZIO[Environment with ZIOAppArgs with Scope,Any,Any] =
    Server.start(
      port = 8080,
      http = DiceRollingApp()
    )
}

case class PlayOutcome(numbers: Rolls, outcome: Outcome)
object PlayOutcome {
  implicit val outcomeEncoder: JsonEncoder[Outcome] = new JsonEncoder[Outcome] {
    override def unsafeEncode(a: Outcome, indent: Option[Int], out: Write): Unit = {
      val asString = a match {
        case Outcome.Bad => "Bad"
        case Outcome.Success(SuccessLevel.Partial) => "Partial"
        case Outcome.Success(SuccessLevel.Full) => "Full"
        case Outcome.Success(SuccessLevel.Critical) => "Critical"
      }
      implicitly[JsonEncoder[String]].unsafeEncode(asString, indent, out)
    }
  }

  implicit val encoder: JsonEncoder[PlayOutcome] =
    DeriveJsonEncoder.gen[PlayOutcome]
}

object DiceRollingApp {
  private implicit val listEncoder: JsonEncoder[List[Int]] = DeriveJsonEncoder.gen[List[Int]]

  def apply(): Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {
      case Method.GET -> !! / "roll" / n  =>
        val result = Roller.randomNumbers(n.toInt)
        Response.json(result.toJson)
      case Method.GET -> !! / "play" / n  =>
        val numbers = Roller.randomNumbers(n.toInt)
        val outcome = morphir.example.app.ScumAndVillainy.outcomeOfRolls(numbers)
        val playOutcome = new PlayOutcome(numbers, outcome)
        Response.json(playOutcome.toJson)
    }
}
