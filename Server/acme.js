const request = require('request-promise')
const _ = require('lodash')
const moment = require('moment')

var URL = 'https://localhost:3000/api'

function rawQuery(bearer, query) {
  const URL = 'query'
  return request.post({
    url: URL,
    body: JSON.stringify(query),
    headers: { 
      'Authorization': 'Bearer ' + bearer,
      'Content-Type': 'application/json'
    }
  })
}

function createUser(user) {
  URL += '/register';
  console.log('URL --- ' + URL)
  return request.post({
    url: URL,
    body: JSON.stringify({user}),
    headers: { 
      'Content-Type': 'application/json'
    }
  })
}

function getUser(bearer, id) {
  URL += '/user/' + id;
  return request.get({
    url: URL,
    headers: { 
    }
  })
}

module.exports = {
  query: rawQuery,
  createUser,
  getUser,
}