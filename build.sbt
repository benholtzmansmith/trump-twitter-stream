name := "trump-twitter-stream"

version := "0.0.1"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-streaming_2.11" % "1.2.1",
  "org.apache.spark" % "spark-streaming-twitter_2.11" % "1.2.1"
)