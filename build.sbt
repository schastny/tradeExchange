name := "TradeExchange"

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.1",
  "org.apache.tika" % "tika-parsers" % "1.5",
  "org.docx4j" % "docx4j" % "3.1.0",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.4"
)