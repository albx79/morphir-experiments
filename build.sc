import mill._, scalalib._

object dist extends ScalaModule {
  def ivyDeps = Agg(ivy"org.morphir::morphir-sdk-core:0.10.0")
  def scalaVersion = "2.13.10"
}