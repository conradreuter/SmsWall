module.exports = {
  load: loadMessages,
  onChange: addChangeCallback,
  get: getMessages,
  put: putMessage,
  delete: deleteMessage,
  getWelcome: getWelcome
};

const FILENAME = 'messages.txt';
const WELCOME_FILENAME = 'welcome.txt';
const fs = require('fs');
const _ = require('lodash');

var nextId = 1;
var messages = [];
var changeCallbacks = [];

function loadMessages(callback) {
  fs.readFile(FILENAME, function(err, data) {
    messages = (!err) ? JSON.parse(data) : [];
    ids = _.map(messages, 'id');
    nextId = 1 + Math.max(0, ...ids);
    callback(messages);
  });
}

function saveMessages(callback) {
  var data = JSON.stringify(messages);
  fs.writeFile(FILENAME, data, function(err) {
    if (err) console.log(err);
    callback();
    _.forEach(changeCallbacks, function(cb) { cb(messages); });
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
  saveMessages(_.partial(callback, message));
}

function deleteMessage(id, callback) {
  var ids = _.map(messages, 'id');
  var index = ids.indexOf(id);
  if (index > -1) {
    var message = messages[index];
    messages = _.without(messages, message);
    saveMessages(_.partial(callback, message));
  } else {
    callback(undefined);
  }
}

function getWelcome(callback) {
  fs.readFile(WELCOME_FILENAME, function(err, data) {
    var welcomeMessage = (!err) ? data.toString() : 'SMS Wall';
    callback(welcomeMessage);
  });
}
