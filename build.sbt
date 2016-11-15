name := "trump-twitter-stream"

version := "0.0.1"

scalaVersion := "2.11.6"

fork := true

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % "2.0.0",
  "org.apache.bahir" %% "spark-streaming-twitter" % "2.0.0",
  "org.apache.spark" %% "spark-sql" % "2.0.0",
  "com.typesafe.play"%% "play-json" % "2.5.9" exclude("com.fasterxml.jackson.core", "jackson-databind"),
  "org.apache.spark" %% "spark-mllib" % "2.0.0"
)