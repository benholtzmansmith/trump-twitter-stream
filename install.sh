#!/usr/bin/env bash

docker pull benhsmith/trump-twitter-stream
docker run -it -p 80:3000 benhsmith/trump-twitter-stream:latest