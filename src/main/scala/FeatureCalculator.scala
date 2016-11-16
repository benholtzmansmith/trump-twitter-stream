package trump.twitter

import org.apache.spark.mllib.feature.HashingTF

object FeatureCalculator {
  val featureCalculators:List[FeatureCalculator] = List(TweetLength)

  val featureCalculatorsMulti:List[FeatureCalculatorMulti] = List(TwoGramTweet)
}

trait FeatureCalculator {
  def calculate(twitterData: TwitterDataRaw):Double
}

trait FeatureCalculatorMulti {
  def calculate(twitterData: TwitterDataRaw):Seq[Double]
}

case object TweetLength extends FeatureCalculator {
  def calculate(twitterData: TwitterDataRaw): Double = twitterData.text.length
}

object Hasher {
  val numFeatures = 1000
  val hashTermFrequency = new HashingTF(numFeatures)
}

case object TwoGramTweet extends FeatureCalculatorMulti {
  def calculate(twitterData: TwitterDataRaw): Seq[Double] =
    Hasher.
      hashTermFrequency.
      transform(twitterData.text.sliding(2).toSeq).
      toArray
}


