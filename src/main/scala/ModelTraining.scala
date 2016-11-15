package trump.twitter

import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import play.api.libs.json.{Format, JsString, Json, Reads}
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import play.api.libs.json.Json.fromJson

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
      as[TwitterData].
      map{
        t =>
          LabeledPoint(
            LabelGenerator.toLabel(t),
            new SparseVector(
              size = 1,
              indices = Array(0),
              Array(t.text.size)
            )
          )
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

case class ModelPerformance(precision:Double, recall:Double, others:Double)

case class TwitterData(polarity:Int, text:String)
object TwitterData {
  implicit val format:Format[TwitterData] = Json.format[TwitterData]
}

object Serialization {
  def deserialize[T:Format](json:String) = Json.fromJson[T](Json.parse(json))
}

object LabelGenerator {
  def toLabel(tweetData: TwitterData):Double = {
    tweetData.polarity match {
      case 0 => 0.0
      case 2 => 1.0
      case 4 => 2.0
    }
  }
}