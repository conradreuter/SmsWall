const os = require('os');
const _ = require('lodash');

module.exports = function getIpAddresses() {
  return _.chain(os.networkInterfaces())
          .map(extractIpAddresses)
          .flatten()
          .value();
}

function extractIpAddresses(addresses, interface) {
  return _.chain(addresses)
          .reject('internal')
          .filter({family: 'IPv4'})
          .map('address')
          .map(_.partial(createInterfaceAddressPair, interface))
          .value()
}

function createInterfaceAddressPair(interface, address) {
  return {
    interface: interface,
    address: address
  };
}
