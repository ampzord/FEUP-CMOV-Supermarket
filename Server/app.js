var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var cryp = require('./crypto_utils');
const { generateKeyPairSync } = require('crypto');
const { writeFileSync, fs } = require('fs');

//const uuid = require('uuid/v4')
//const session = require('express-session')

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');
var transactionsRouter = require('./routes/transactions');

var app = express();

//only runs once for the keys only need to be generated once
//cryp.generateKeys();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);

//Server and user communication
app.use('/api', usersRouter); //register
app.use('/api', transactionsRouter); //register



module.exports = app;
