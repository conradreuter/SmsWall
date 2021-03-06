const _ = require('lodash');
const messages = require('./messages');
const getIpAddresses = require('./ip-addresses');

const app = require('express')();
const server = require('http').Server(app);
const io = require('socket.io')(server);
app.use(require('body-parser').text());

app.get('/', function(req, res) {
  res.sendFile(__dirname + '/index.html');
});

app.get('/ping', function(req, res) {
  console.log('Received a ping from ' + req.ip + '.');
  res.send('pong');
});

app.get('/message', function(req, res) {
  messages.get(_.bindKey(res, 'send'));
});

app.put('/message', function(req, res) {
  var text = req.body;
  messages.put(text, function(msg) {
    console.log('New message: ' + msg.text);
    res.status(201).type('json').send(msg);
  });
});

app.delete('/message/:id', function(req, res) {
  var id = parseInt(req.params.id);
  messages.delete(id, function(msg) {
    if (msg === undefined) {
      res.status(404).send();
    } else {
      console.log('Deleted message: ' + msg.text);
      res.status(200).type('json').send(msg);
    }
  });
});

var welcomeMessage = 'SMS Wall';
app.put('/welcome', function(req, res) {
  welcomeMessage = req.body;
  console.log('New welcome message: ' + welcomeMessage);
  io.emit('welcome', welcomeMessage);
  res.status(200).send();
});

messages.onChange(function(msgs) {
  io.emit('messages', msgs);
});

messages.load(function(msgs) {
  console.log('Loaded ' + msgs.length + ' message(s).');
  server.listen(8080);
  _.forEach(getIpAddresses(), function(pair) {
    console.log('Found IP address ' + pair.address +
                ' on interface ' + pair.interface +  '.');
  });
  io.on('connection', function(socket) {
    socket.emit('messages', msgs);
    messages.getWelcome(socket.emit.bind(socket, 'welcome'));
  })
});
