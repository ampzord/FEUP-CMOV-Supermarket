const Joi = require('joi');
const express = require('express');
const router = express.Router();
const { check, validationResult } = require('express-validator')
const { User } = require('../database')
const { Transaction } = require('../database')
const uuidv4 = require('uuid/v4');
const cryp = require('../crypto_utils');
const forge = require('node-forge');
const NodeRSA = require('node-rsa');
const bcrypt = require('bcrypt');
const saltRounds = 10;

const registrationValidationRules = [
  check('fName','First name is empty.').not().isEmpty(),
  check('lName','Last name is empty.').not().isEmpty(),
  check('username','Username is empty.').not().isEmpty(),
  check('password','Password is empty.').not().isEmpty(),
  check('password_conf','Password confirmation is empty.').not().isEmpty(),
  check('credit_card_name','Credit card name is empty.').not().isEmpty(),
  check('credit_card_number','Credit card number is empty.').not().isEmpty(),
  check('credit_card_exp_date','Credit card date is empty.').not().isEmpty(),
  check('credit_card_cvc','Credit card cvc is empty.').not().isEmpty(),
  check('fName','First name does not meet minimum length (3).').isLength({ min: 3}),
  check('lName','Last name does not meet minimum length (3).').isLength({ min: 3}),
  check('username','Username is not alphanumeric.').isAlphanumeric(),
  check('username','Username does not meet minimum length (3).').isLength({ min: 3}),
  check('username','Username does not meet minimum length (3).').isLength({ min: 3}),
  check('password', 'Password does not meet minimum length (3).').isLength({ min: 3 }),
  check('password_conf', 'Passwords do not match.').custom((value, {req}) => (value === req.body.password)),
  check('credit_card_number', 'Credit card must be 16 length number.').isLength(16),
  check('credit_card_cvc', 'Credit card must be 3 length number. (VISA)').isLength(3),

]

/* Register user in database */
router.post('/register', registrationValidationRules, function(req, res, next) {

  console.log(req.body);
  const errors = validationResult(req);

  /*//Load server public key from file

  // convert PEM-formatted private key to a Forge private key
  var forgePrivateKey = forge.pki.privateKeyFromPem(cryp.server_private_key);

  // get a Forge public key from the Forge private key
  var forgePublicKey = forge.pki.setRsaPublicKey(forgePrivateKey.n, forgePrivateKey.e);

  // convert the Forge public key to a PEM-formatted public key
  var publicKey = forge.pki.publicKeyToPem(forgePublicKey);

  // convert the Forge public key to an OpenSSH-formatted public key for authorized_keys
  //var sshPublicKey = forge.ssh.publicKeyToOpenSSH(forgePublicKey);*/

  if (errors.isEmpty()) {
    var salt = bcrypt.genSaltSync(saltRounds);
    var hash = bcrypt.hashSync(req.body.password, salt);
    // Create a new user
    User.create({
      fName: req.body.fName,
      lName: req.body.lName,
      username: req.body.username,
      password: hash,
      credit_card_number: req.body.credit_card_number,
      credit_card_name: req.body.credit_card_name,
      credit_card_exp_date: req.body.credit_card_exp_date,
      credit_card_cvc: req.body.credit_card_cvc,
      public_key: req.body.public_key,
      uuid: uuidv4(),
    }).then(user => {
      //delete user.password;
      //delete user.password_conf;

      res.status(200).json({
        ok: true,
        user: user,
        message: 'User created successfully.',
        server_public_key: cryp.server_certificate,
      })
    }).catch(err => {
      console.log(err.errors[0].message);
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

  console.log(req.body);

  const errors = validationResult(req);

  if (errors.isEmpty()) {
    User.findOne({
      where: {username: req.body.username}
    })
        .then((user) => {
          //check if user exists
          if (!user) {
            console.log("Username: " + req.body.username + " doesn't exist.");
            return res.status(404).send("Username: " + req.body.username + " doesn't exist.");
          }

          //check if password matches
          if (comparePasswords(req.body.password, user.password)) {
            console.log("Password is incorrect.");
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

function comparePasswords(passwordAttempt, hash) {
  bcrypt.compare(passwordAttempt, hash, (err, res) => {
    if (res == true) {
      //console.log('Password is the same.', res, hash)
      return true;
    }
    else {
      //console.log('Password is different.', res, hash)
      return false;
    }
  })
}


router.post('/transaction', function(req, res, next) {
  console.log(req.body);

  // when the server receives a voucher in a payment he should
  // check if it belongs to the right user AND was not used.


  //parse transaction
  Transaction.create({
    uuid: req.body.fName,
    user_uuid: req.body.lName,
    price: req.body.username,
    products_size: hash,
    credit_card_number: req.body.credit_card_number,
    credit_card_name: req.body.credit_card_name,
  }).then(user => {

    res.status(200).json({
      ok: true,
      user: user,
      message: 'User created successfully.',
      server_public_key: cryp.server_certificate,
    })
  }).catch(err => {
    console.log(err.errors[0].message);
    res.status(500).json('Username already exists.');
  });

  //generate voucher depending on cost

});

/** All transactions of a user of a given UUID */
router.get('/transactions/:uuid', function(req, res, next) {
  console.log(req.body);
  Transaction.findAll({
    where: {
      user_uuid: req.params.uuid
    }
  }).then(transactions => res.json(transactions));
});


/* GET all users listing. */
router.get('/users', function(req, res, next) {
  User.findAll().then(users => res.json(users))
});

module.exports = router;


