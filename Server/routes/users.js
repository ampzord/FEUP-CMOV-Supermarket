var express = require('express');
var router = express.Router();
const { check, validationResult } = require('express-validator/check')

const validationRules = [
  check('email').isEmail(),
  check('password').isLength({ min: 6 }),
  check('username').isAlphanumeric(),
]

/* GET users listing. */
router.get('/', function(req, res, next) {
  res.json({ name: "Antonio", age: "18", email: "asd@gmail.com"});
});

/* POST signup page. */
router.post('/', validationRules, (req, res, next) => {
  console.log(req.body)
  console.log((validationResult(req).array()))
  return res.render('signup', { title: 'PicoBlog' });;
});

module.exports = router;


