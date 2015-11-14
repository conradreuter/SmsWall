module.exports = {
  load: loadMessages,
  onChange: addChangeCallback,
  get: getMessages,
  put: putMessage,
  delete: deleteMessage
};

const FILENAME = 'messages.txt';
const fs = require('fs');

var nextId = 1;
var messages = [];
var changeCallbacks = [];

function loadMessages(callback) {
  fs.readFile(FILENAME, function(err, data) {
    messages = (!err) ? JSON.parse(data) : [];
    ids = messages.map(function(msg) { return msg.id; });
    nextId = 1 + Math.max(0, ...ids);
    callback(messages);
  });
}

function saveMessages(callback) {
  var data = JSON.stringify(messages);
  fs.writeFile(FILENAME, data, function(err) {
    if (err) console.log(err);
    callback();
    changeCallbacks.forEach(function(cb) { cb(messages); });
  });
}

function addChangeCallback(callback) {
  changeCallbacks.push(callback);
}

function getMessages(callback) {
  callback(messages);
}

function putMessage(text, callback) {
  var message = {
    id: nextId++,
    text: text,
    timestamp: Date.now()
  };
  messages.push(message);
  saveMessages(function() {
    callback(message);
  });
}

function deleteMessage(id, callback) {
  var ids = messages.map(function(msg) { return msg.id; });
  var index = ids.indexOf(id);
  if (index > -1) {
    var message = messages[index];
    messages = messages.filter(function(msg) { return msg !== message; });
    saveMessages(function() {
      callback(message);
    });
  } else {
    callback(undefined);
  }
}
