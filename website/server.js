const express = require('express');
const app = express();
const http = require('http').Server(app);
const io = require('socket.io')(http);
const bodyParser = require('body-parser')

app.use(express.static('.'));

app.use(bodyParser.json())

app.get('/', function (req, res) {
  res.sendFile('index.html', {root: __dirname });
});

app.post('/predict', function (req, res) {
  console.log(req.body)
  io.sockets.emit('tweets',req.body)
  res.send("ok")
});


io.on('connection', function(socket){
  console.log('Connection');
});

var port = process.env.PORT || 3000

http.listen(port, function(){
  console.log('listening for requests on *:' + port);
});
