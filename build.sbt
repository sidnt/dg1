val scalaV = "2.13.4"
val zioV = "1.0.4-2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dg1",
    version := "0.1.0",
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioV,
      "dev.zio" %% "zio-test" % zioV % "test",
      "dev.zio" %% "zio-test-sbt" % zioV % "test",
      "dev.zio" %% "zio-json" % "0.1",
      "io.dgraph" % "dgraph4j" % "20.11.0"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
