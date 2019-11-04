const Joi = require('joi');
const express = require('express');
const router = express.Router();
const { check, validationResult, signupFailures } = require('express-validator')
const { User } = require('../database')
const uuidv4 = require('uuid/v4');
const keys = require('../cryptography');
const { generateKeyPair } = require('crypto');
const { writeFileSync, fs } = require("fs");

const registrationValidationRules = [
  check('fName','First name is empty.').not().isEmpty(),
  check('lName','Last name is empty.').not().isEmpty(),
  check('username','Username is empty.').not().isEmpty(),
  check('password','Password is empty.').not().isEmpty(),
  check('password_conf','Password confirmation is empty.').not().isEmpty(),
  check('credit_card','Credit card is empty.').not().isEmpty(),
  check('fName','First name is empty.').not().isEmpty(),
  check('fName','First name is not alphanumeric.').isAlphanumeric(),
  check('fName','First name does not meet minimum length (3).').isLength({ min: 3}),
  check('lName','Last name is not alphanumeric.').isAlphanumeric(),
  check('lName','Last name does not meet minimum length (3).').isLength({ min: 3}),
  check('username','Username is not alphanumeric.').isAlphanumeric(),
  check('username','Username does not meet minimum length (3).').isLength({ min: 3}),
  check('username','Username does not meet minimum length (3).').isLength({ min: 3}),
  check('password', 'Password does not meet minimum length (3).').isLength({ min: 3 }),
  check('password_conf', 'Passwords do not match.').custom((value, {req}) => (value === req.body.password)),
  check('credit_card', 'Credit card must be 16 length number.').isLength(16),
]

/* Register user in database */
router.post('/register', registrationValidationRules, function(req, res, next) {
  //App sends to server:
    // - Registration information
    // - App public key

  //Server must send to App:
    // - UUID
    // - Server public key

  console.log(req.body);
  const errors = validationResult(req);
  const uuid = uuidv4();

  //Load server public key from file

  if (errors.isEmpty()) {
    const user = new User(req.body);
    user.save()
    .then(user => {
        res.status(200).json({ //send UUI & server public key
          ok: true,
          user: user,
          message: 'User created successfully.',
          server_public_key: 'publickeywaiting',
          uuid: uuid
        })
    })
    .catch(err => {
        res.status(500).json('Username already exists.');
    });
  }
  else {
    return res.status(422).jsonp(errors.array()[0].msg);
  }
  

 
});

const signinValidationRules = [
  check('username','Username is empty.').not().isEmpty(),
  check('password','Password is empty.').not().isEmpty(),
]

/* Sign in user */
router.post('/login', signinValidationRules, function(req, res, next) {

  const errors = validationResult(req);

  if (errors.isEmpty()) {
    User.findOne({
      where: {username: req.body.username}
    })
    .then((user) => {
      //console.log(user.dataValues);

      //check if user exists
      if (!user) {
        //console.log("Username: " + req.body.username + " doesn't exist.")
        return res.status(404).send("Username: " + req.body.username + " doesn't exist.");
      }

      //check if password matches
      if (!checkPassword(req.body.password, user.password)) {
        //console.log("Password is incorrect.");
        return res.status(404).send("Password is incorrect.");
      }

      //SIGN IN SUCCESSFUL
      res.status(200).json({
        ok: true,
        user: user,
        message: 'User successfully logged in.'
      })
  })
  } else {
    return res.status(422).jsonp(errors.array()[0].msg);
  }
});

function checkPassword(message_pwd, db_password) {
  if (message_pwd === db_password)
    return true;
  return false;
}




/* GET all users listing. */
router.get('/users', function(req, res, next) {
  User.findAll().then(users => res.json(users))
});

module.exports = router;


