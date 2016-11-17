package trump.twitter

import org.slf4j.LoggerFactory
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Milliseconds, Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import play.api.libs.json.{Format, Json}

import scalaj.http.Http

object Stream {
  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]) {

    logger.error("Starting streaming application")

    val pathToModel = args(0)

    val nodePort = sys.env.get("PORT").getOrElse("3000")

    logger.error(s"Connecting to node port ${nodePort}")

    val sparkConf = new SparkConf().
      setMaster("local[*]").
      setAppName("spark-twitter-stream-example")

    val sc = new SparkContext(sparkConf)

    val streamingContext = new StreamingContext(sc, Seconds(1))

    val model = LogisticRegressionModel.load(sc, pathToModel)

    TwitterUtils.
      createStream(streamingContext, None, Seq("Trump", "trump")).
      map{ tweet =>
        val features = TwoGramTweet.calculate(TwitterDataRaw(tweet.getText))

        val prediction = model.predict(Vectors.dense(features.toArray))

        val typedPrediction = TweetDataPredicted.toTypedLabel(prediction)

        postToNodeServer(TweetDataPredicted(text = tweet.getText, sentiment = typedPrediction.asString), nodePort)
      }.print()

    streamingContext.start()

    streamingContext.awaitTermination()
  }

  def postToNodeServer(tweetData: TweetDataPredicted, port:String):Unit = {
    Http(s"http://localhost:${port}/predict").
      postData(Json.toJson(tweetData).toString()).
      header("Content-Type", "application/json").
      header("Charset", "UTF-8").
      asString.
      code
  }
}

trait Sentiment extends Product {
  def asString:String = this.productPrefix
}
case object Positive extends Sentiment
case object Neutral extends Sentiment
case object Negative extends Sentiment

case class TweetDataPredicted(text:String, sentiment:String)

object TweetDataPredicted {
  implicit val format:Format[TweetDataPredicted] = Json.format[TweetDataPredicted]

  def toTypedLabel(prediction:Double):Sentiment = {
    prediction match {
      case 0.0 => Negative
      case 1.0 => Neutral
      case 2.0 => Positive
    }
  }
}
