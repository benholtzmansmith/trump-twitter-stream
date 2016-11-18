#!/usr/bin/env bash

npm start & spark-submit --master local[*] --conf spark.driver.memory=4G --class trump.twitter.Stream /app/trump-twitter-stream-assembly-0.0.1.jar /app/model >/dev/null

