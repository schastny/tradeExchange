name := "TenderParser"

version := "1.0"

libraryDependencies ++= Seq(
  "org.apache.tika" % "tika-parsers" % "1.5",
  "org.docx4j" % "docx4j" % "3.1.0",
  "com.lowagie" % "itext" % "4.2.1",
  "com.artofsolving" % "jodconverter" % "2.2.1",
  "com.artofsolving" % "jodconverter-core" % "3.0-beta-4"
)

resolvers +=
  "JODs" at "http://logicaldoc.sourceforge.net/maven/"