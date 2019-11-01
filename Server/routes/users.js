const Joi = require('joi');
const express = require('express');
const router = express.Router();
const { check, validationResult, signupFailures } = require('express-validator')
const { User } = require('../database')

const validationRules = [
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
router.post('/register', validationRules, function(req, res, next) {
  //check if username is unique
  //gets public key from user (App)

  console.log(req.body);

  const errors = validationResult(req);

  if (!errors.isEmpty()) {
    return res.status(422).jsonp(errors.array()[0].msg);
  } else {
    const user = new User(req.body);
  user.save()
  .then(user => {
      res.status(200).json({
        ok: true,
        user: user,
        message: 'User created successfully.'
      })
  })
  .catch(err => {
      res.status(500).json('Username already exists');
  });
  }
  
});

/* GET all users listing. */
router.get('/users', function(req, res, next) {
  User.findAll().then(users => res.json(users))
});

/* Get User from ID */
router.get('/user/:id', function(req, res) {
  const user = User.findByPk(req.params.id);
  if (!user)
      return res.status(404).send('The user with the given ID was not found');
  res.send(user);
});

module.exports = router;


