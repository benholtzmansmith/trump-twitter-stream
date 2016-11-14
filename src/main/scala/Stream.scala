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
      map{tweet =>
        val sentiment = getSentiment(tweet.getText)
        postToNodeServer(TweetData(text = tweet.getText, sentiment = sentiment.asString))
      }.
      print()

    streamingContext.start()
  }

  def getSentiment(tweet:String):Sentiment = {
    ???
  }

  def postToNodeServer(tweetData: TweetData):Unit = {
    ???
  }
}

trait Sentiment {
  def asString:String = this.getClass.getSimpleName
}
case object Positive extends Sentiment
case object Neutral extends Sentiment
case object Negative extends Sentiment

case class TweetData(text:String, sentiment:String)
