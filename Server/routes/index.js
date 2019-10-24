var express = require('express');
var router = express.Router();

const utils = require('../utils')
const { User } = require('../database')

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

router.get('/test', function(req, res) {
  res.status(200).send('Connection successful')
})

module.exports = router;
