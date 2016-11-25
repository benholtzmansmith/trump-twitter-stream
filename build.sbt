name := "trump-twitter-stream"

version := "0.0.1"

scalaVersion := "2.11.6"

fork := true

val sparkVersion = "2.0.1"

libraryDependencies ++= Seq(
  "org.apache.spark"       %% "spark-streaming"         % sparkVersion,
  "org.apache.bahir"       %% "spark-streaming-twitter" % sparkVersion,
  "org.apache.spark"       %% "spark-sql"               % sparkVersion,
  "org.apache.spark"       %% "spark-mllib"             % sparkVersion,
  "com.typesafe.play"      %% "play-json"               % "2.5.9" exclude("com.fasterxml.jackson.core", "jackson-databind"),
  "org.scalaj"             %% "scalaj-http"             % "2.3.0"
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case PathList("org", "apache", "spark", "unused", _*)      => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}