package app

import morphir.example.app.ScumAndVillainy.{Outcome, Rolls, SuccessLevel, outcomeOfRolls}
import service.Roller
import zhttp.http._
import zhttp.service.Server
import zio.cli.HelpDoc.Span.text
import zio.cli.{Args, CliApp, Command, ZIOCliDefault}
import zio.json.internal.Write
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
import zio.{Console, Scope, ZIO, ZIOAppArgs, ZLayer}

import scala.io.Source

object Main extends ZIOCliDefault {

  private lazy val serve = Command("serve")
  private lazy val numOfDice = Args.integer.atLeast(0) ?? "The number of dice to roll"
  private lazy val play = Command("play", numOfDice)
  private lazy val cmd = Command("mesv").subcommands(serve, play)

  override def cliApp: CliApp[Any with ZIOAppArgs with Scope, Any, Any] = CliApp.make(
    name = cmd.names.head,
    version = "0.1.0",
    summary = text("Morphir Experiments for Scum&Villainy"),
    command = cmd
  ) {
    case (n: BigInt) :: Nil => for {
      rolls <- ZIO.succeed(Roller.randomNumbers(n.intValue))
      outcome <- ZIO.succeed(outcomeOfRolls(rolls))
      _ <- Console.printLine(s"Rolled $rolls which is a $outcome")
    } yield ()
    case () => httpWorkflow.provide(ZLayer.succeed(HttpServerConfig(8080)))
  }

  lazy val getConfig: ZIO[Any, Throwable, AppConfig] = ZIO.scoped {
    ZIO.fromAutoCloseable(ZIO.attemptBlocking(Source.fromFile("./dist/conf.json")))
      .map(_.mkString.fromJson[AppConfig])
      .flatMap(e => ZIO.fromEither(e).mapError(e => new Exception(e)))
  }

  lazy val httpWorkflow: ZIO[HttpServerConfig, Throwable, Unit] = for {
    config <- getConfig
    config <- ZIO.log(s"Application started with $config") *> ZIO.service[HttpServerConfig].provide(ZLayer.succeed(config.server))
    server <- ZIO.log(s"Serving on port ${config.port}") *> Server.start(
      port = config.port,
      http = DiceRollingApp()
    )
  } yield server
}

case class HttpServerConfig(port: Int) extends Serializable

object HttpServerConfig {
  implicit val decoder: JsonDecoder[HttpServerConfig] = DeriveJsonDecoder.gen[HttpServerConfig]
}

case class AppConfig(server: HttpServerConfig)

object AppConfig {
  implicit val decoder: JsonDecoder[AppConfig] = DeriveJsonDecoder.gen[AppConfig]
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
      case Method.GET -> !! / "roll" / n =>
        val result = Roller.randomNumbers(n.toInt)
        Response.json(result.toJson)
      case Method.GET -> !! / "play" / n =>
        val numbers = Roller.randomNumbers(n.toInt)
        val outcome = morphir.example.app.ScumAndVillainy.outcomeOfRolls(numbers)
        val playOutcome = new PlayOutcome(numbers, outcome)
        Response.json(playOutcome.toJson)
    }
}
