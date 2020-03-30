name := "zio-test"
version := "0.1"
scalaVersion := "2.13.1"

scalacOptions ~= filterConsoleScalacOptions

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.1" % Test
)
