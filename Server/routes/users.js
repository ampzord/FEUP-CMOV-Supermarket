const Joi = require('joi');
const express = require('express');
const router = express.Router();
const { check, validationResult, signupFailures } = require('express-validator')
const { User } = require('../database')

const { validateRegister } = require('./validator.js')

const validationRules = [
  check('name').isAlphanumeric(),
  check('name').isLength({ min: 3 }),
  check('username').isAlphanumeric(),
  check('username').isLength({ min: 3 }),
  check('password').isLength({ min: 6 }),
  check('password_conf').isLength({ min: 6 }),
]

/* Register user in database */
router.post('/register', validateRegister, function(req, res, next) {
  //check if username is unique
  //validate inputs
  //gets public key from user (App)
  //return user()

  var errors = validationResult(req).formatWith(signupFailures);

  if (!errors.isEmpty()) {
      res.status(400).json(errors.array());
  } else {
      res.sendStatus(200);
  }

  const user = new User(req.body);
  user.save()
  .then(user => {
      res.json('User added successfully.');
  })
  .catch(err => {
      res.status(400).send("Unable to save to database.");
  });

  
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



/* POST signup page. */
/*
router.post('/asdada', validationRules, (req, res, next) => {
  console.log(req.body)
  console.log((validationResult(req).array()))
  return res.render('signup', { title: 'PicoBlog' });;
});
*/
module.exports = router;


