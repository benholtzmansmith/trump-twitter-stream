# trump-twitter-stream

viewable at poliai.com

This is a real time streaming service that runs sentiment analysis on tweets with Trump mentions.

I trained a logistic regression model on the tweet polarity data provideded here: http://help.sentiment140.com/for-students/

The model is trained using 3 classes (Neutral, Negative, Positive) using Spark 2.0

The node and spark scala app are dockerized in the same container.
