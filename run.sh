#!/usr/bin/env bash

npm start & spark-submit --master local[*] --class trump.twitter.Stream /app/trump-twitter-stream-assembly-0.0.1.jar /app/model >> spark-error.tex

