package trump.twitter

import org.apache.spark._
import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS, NaiveBayes}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SQLContext}
import play.api.libs.json.{Format, Json}

/**
  * Trained on http://help.sentiment140.com/for-students/
  * */

object ModelTraining {
  def main(args: Array[String]) {
    def pathToTrainingData = args(0)

    val sparkConf = new SparkConf().
      setAppName("model-training").
      setMaster("local")

    val sc = new SparkContext(sparkConf)

    val spark = new SQLContext(sc)

    import spark.implicits._

    val twitterSchema = StructType(
      Array(
        StructField("polarity", IntegerType, true),
        StructField("id", StringType, true),
        StructField("date", StringType, true),
        StructField("query", StringType, true),
        StructField("user", StringType, true),
        StructField("text", StringType, true)
      )
    )

    val df: DataFrame = spark
      .read
      .schema(twitterSchema)
      .csv(pathToTrainingData)

    val data = df.
      as[TwitterDataLabel].
      map{
        t =>
          val features = TwoGramTweet.calculate(TwitterDataRaw.fromLabel(t))

          val vector = Vectors.dense( features.toArray )

          LabeledPoint(LabelGenerator.toDoubleLabel(t), vector)

      }.
      randomSplit(Array(.7, .3), seed = 11L)

    val (train, test) = (data(0), data(1))

    val logisticRegressionWithLBFGS = new LogisticRegressionWithLBFGS

    val logisticRegressionModel: LogisticRegressionModel =
      logisticRegressionWithLBFGS.setNumClasses(3).run(train.rdd)

    val predictionAndLabels = test.rdd.map { case LabeledPoint(label, features) =>
      val prediction = logisticRegressionModel.predict(features)
      (prediction, label)
    }

    val metrics = new MulticlassMetrics(predictionAndLabels)

    val accuracy = metrics.accuracy

    println(s"Accuracy = $accuracy")

    logisticRegressionModel.save(sc, "target/tmp/scalaLogisticRegressionWithLBFGSModel")

  }
}
  case class TwitterDataRaw(text:String)

  object TwitterDataRaw {
    def fromLabel(twitterDataLabel: TwitterDataLabel) = TwitterDataRaw(twitterDataLabel.text)
  }

case class TwitterDataLabel(polarity:Int, text:String)

object LabelGenerator {

  //(0 = negative, 2 = neutral, 4 = positive)
  def toDoubleLabel(tweetData: TwitterDataLabel):Double = {
    tweetData.polarity match {
      case 0 => 0.0
      case 2 => 1.0
      case 4 => 2.0
    }
  }

}