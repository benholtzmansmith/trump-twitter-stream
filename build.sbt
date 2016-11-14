name := "trump-twitter-stream"

version := "0.0.1"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-streaming_2.11" % "1.6.2",
  "org.apache.spark" % "spark-streaming-twitter_2.11" % "1.6.2",
  "org.apache.spark" % "spark-sql_2.11" % "1.6.2",
  "com.databricks" % "spark-csv_2.11" % "1.5.0",
  "com.typesafe.play" % "play-json_2.11" % "2.5.9" exclude("com.fasterxml.jackson.core", "jackson-databind"),
  "org.apache.spark" % "spark-mllib_2.11" % "1.6.2"
)