package trump.twitter

import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import play.api.libs.json.{Format, JsString, Json, Reads}
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import play.api.libs.json.Json.fromJson

object ModelTraining {
  def main(args: Array[String]) {
    def pathToTrainingData = args(0)

    val sparkConf = new SparkConf().
      setAppName("model-training").
      setMaster("local")

    val sc = new SparkContext(sparkConf)

    val sqlContext = new SQLContext(sc)

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

    val df: DataFrame = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("inferSchema", "true") // Automatically infer data types
      .schema(twitterSchema)
      .load(pathToTrainingData)

    val data = df.toJSON.
      flatMap{ t =>
        Serialization.deserialize[TwitterData](t).asOpt
      }.
      map{
        t =>
          LabeledPoint(
            LabelGenerator.toLabel(t),
            new SparseVector( size = 1, indices = Array(1), Array(t.polarity.toDouble) )
          )
      }.
      randomSplit(Array(.7, .3), seed = 11L)

    val (train, test) = (data(0), data(1))

    val logisticRegressionWithLBFGS = new LogisticRegressionWithLBFGS

    val logisticRegressionModel: LogisticRegressionModel =
      logisticRegressionWithLBFGS.run(train)


    val testResults = testPerformance(logisticRegressionModel, test)

    println(
      s"""
        |Test results:
        |
        |Precision:${testResults.precision}
        |Recall:${testResults.recall}
        |Fraction of things that weren't classified in performance:${testResults.others}
        |
      """.stripMargin)
  }

  def testPerformance(model: LogisticRegressionModel, testData: RDD[LabeledPoint]): ModelPerformance = {
    val modelPredictions = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    val (tpCount, fnCount, allOthers, count) = modelPredictions.aggregate(0.0, 0.0, 0.0, 0.0)({
      case ((truePositiveCount, falseNegativeCount, allOthers, count), (trueLabel, predicted)) =>
        if (trueLabel == predicted)
          (truePositiveCount + 1, falseNegativeCount, allOthers, count +1)
        else if (trueLabel == 1 && trueLabel != predicted) (truePositiveCount, falseNegativeCount + 1, allOthers,count +1)
        else (truePositiveCount, falseNegativeCount, allOthers + 1, count +1)
    }, { case ((tp1, fn1, others1, count), (tp2, fn2, others2, _)) => (tp1 + tp2, fn1 + fn2, others1 + others2, count + 1) })

    val precision = tpCount / count
    val recall = tpCount / (fnCount + tpCount)
    val others = allOthers / count

    ModelPerformance(precision = precision, recall = recall, others = others)
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