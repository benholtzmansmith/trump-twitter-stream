package trump.twitter

import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object Stream {
  def main(args: Array[String]) {

    val sparkConf = new SparkConf().
      setMaster("local[*]").
      setAppName("spark-twitter-stream-example")

    val sc = new SparkContext(sparkConf)

    val streamingContext = new StreamingContext(sc, Milliseconds(100))

    TwitterUtils.
      createStream(streamingContext, None, Seq("Trump")).
      map(_.getText).
      print()

    streamingContext.start()
  }
}
