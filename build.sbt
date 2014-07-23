name := "tradeexchange"

organization := "net.shchastnyi.medical"

version := "1.0"

scalaVersion := "2.11.1"

//Define dependencies. These ones are only required for Test and Integration Test scopes.
libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-library" % "2.11.1",
    "org.apache.tika" % "tika-parsers" % "1.5",
    "org.docx4j" % "docx4j" % "3.1.0",
    "com.typesafe.akka" % "akka-actor_2.11" % "2.3.4",
    "org.scalatest"   %% "scalatest"    % "2.1.6"   % "test,it",
    "org.scalacheck"  %% "scalacheck"   % "1.11.4"      % "test,it"
)

// For Settings/Task reference, see http://www.scala-sbt.org/release/sxr/sbt/Keys.scala.html

// Compiler settings. Use scalac -X for other options and their description.
// See Here for more info http://www.scala-lang.org/files/archive/nightly/docs/manual/html/scalac.html 
scalacOptions ++= List("-feature","-deprecation", "-unchecked", "-Xlint")

// ScalaTest settings.
// Ignore tests tagged as @Slow (they should be picked only by integration test)
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "org.scalatest.tags.Slow", "-u","target/junit-xml-reports", "-oD", "-eS")

//Style Check section 
org.scalastyle.sbt.ScalastylePlugin.Settings
 
org.scalastyle.sbt.PluginKeys.config <<= baseDirectory { _ / "src/main/config" / "scalastyle-config.xml" }

// Generate Eclipse project with sources for dependencies
EclipseKeys.withSource := true
