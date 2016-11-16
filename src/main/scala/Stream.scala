package trump.twitter

import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import play.api.libs.json.Json

object Stream {
  def main(args: Array[String]) {

    val pathToModel = args(0)

    val sparkConf = new SparkConf().
      setMaster("local[*]").
      setAppName("spark-twitter-stream-example")

    val sc = new SparkContext(sparkConf)

    val streamingContext = new StreamingContext(sc, Milliseconds(100))

    val model = LogisticRegressionModel.load(sc, pathToModel)

    TwitterUtils.
      createStream(streamingContext, None, Seq("Trump")).
      map{tweet =>
        val features = TwoGramTweet.calculate(TwitterDataRaw(tweet.getText))

        val prediction = model.predict(Vectors.dense(features.toArray))

        val typedPrediction = TweetDataPredicted.toTypedLabel(prediction)

        postToNodeServer(TweetDataPredicted(text = tweet.getText, sentiment = typedPrediction.asString))
      }.
      print()

    streamingContext.start()
  }

  def postToNodeServer(tweetData: TweetDataPredicted):Unit = {
//    Http("http://jobcoin.projecticeland.net/tractarian/api/transactions").
//      postData(Json.toJson(transaction).toString()).
//      header("Content-Type", "application/json").
//      header("Charset", "UTF-8").
//      asString
  }
}

trait Sentiment {
  def asString:String = this.getClass.getSimpleName
}
case object Positive extends Sentiment
case object Neutral extends Sentiment
case object Negative extends Sentiment

case class TweetDataPredicted(text:String, sentiment:String)
object TweetDataPredicted {
  def toTypedLabel(prediction:Double):Sentiment = {
    prediction match {
      case 0.0 => Negative
      case 1.0 => Neutral
      case 2.0 => Positive
    }
  }
}
