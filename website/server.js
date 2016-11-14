const Twitter = require('twitter');
const express = require('express');
const app = express();
const http = require('http').Server(app);
const io = require('socket.io')(http);

var client = new Twitter({
 consumer_key: process.env.TWITTER_CONSUMER_KEY,
 consumer_secret: process.env.TWITTER_CONSUMER_SECRET,
 access_token_key: process.env.TWITTER_ACCESS_TOKEN_KEY,
 access_token_secret: process.env.TWITTER_ACCESS_TOKEN_SECRET
});

app.use(express.static('.'));

app.get('/', function (req, res) {
  res.sendFile('index.html', {root: __dirname });
});

io.on('connection', function(socket){
  console.log('some connection');
  socket.on('stream', function(msg){
  	client.stream('statuses/filter', {track: 'trump'}, function(stream) {
  	 stream.on('data', function(event) {
  	   console.log(event && event.text);
  	   io.sockets.emit('tweets',event.text)
  	 });

  	 stream.on('error', function(error) {
  	   throw error;
  	 });
  	});
   });
});

http.listen(3000, function(){
  console.log('listening on *:3000');
});