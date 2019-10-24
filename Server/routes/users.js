var express = require('express');
var router = express.Router();
const { check, validationResult } = require('express-validator/check')
const { User } = require('../database')

const validationRules = [
  check('email').isEmail(),
  check('password').isLength({ min: 6 }),
  check('username').isAlphanumeric(),
]

router.get('/register', function(req, res, next) {
  res.json({ name: "Antonio", age: "18", email: "asd@gmail.com"});
});

router.get('/login', function(req, res, next) {
  //User.findAll().then(users => res.json(users))
});

/* GET all users listing. */
router.get('/users', function(req, res, next) {
  User.findAll().then(users => res.json(users))
});






/* POST signup page. */
router.post('/', validationRules, (req, res, next) => {
  console.log(req.body)
  console.log((validationResult(req).array()))
  return res.render('signup', { title: 'PicoBlog' });;
});

module.exports = router;


