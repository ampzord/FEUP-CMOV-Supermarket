const Sequelize = require('sequelize')
const UserModel = require('./models/user')
const TransactionModel = require('./models/transaction')
const VoucherModel = require('./models/voucher')

const sequelize = new Sequelize('acme', 'admin', 'sqladmin', {
  host: 'localhost',
  dialect: 'sqlite',
  //operatorsAliases: false,
  logging: false,

  pool: {
    max: 5,
    min: 0,
    acquire: 30000,
    idle: 10000
  },
  storage: 'database/database.sqlite'
})

const User = UserModel(sequelize, Sequelize)
const Transaction = TransactionModel(sequelize, Sequelize)
const Voucher = VoucherModel(sequelize, Sequelize)

sequelize
  .authenticate()
  .then(() => {
    console.log('SQLite connection has been established successfully.');
  })
  .catch(error => {
    console.error('Unable to connect to the database:', error);
  })

sequelize
  .sync({ force: true })
  .then(() => {
    //console.log(`Database & tables created!`)
    User.bulkCreate([
      { fName: 'Ricardo', lName: 'Fonseca', username: 'rfonseca', password: 'password1', credit_card_number: '4611441749020896', public_key: 'publickey1', uuid: 'ad5053d3-7a11-4de2-ba25-1dfa7768efc2', credit_card_name: 'Fonseca', credit_card_exp_date: '07/21', credit_card_cvc: '345', totalSpent: '0', totalSaved: '0' },
      { fName: 'Miguel', lName: 'Moreira', username: 'mmoreira', password: 'password2', credit_card_number: '4025668462246072', public_key: 'publickey2', uuid: 'a8e66bcc-790b-4d06-91b1-f49b45de6910', credit_card_name: 'Moreira', credit_card_exp_date: '07/21', credit_card_cvc: '345', totalSpent: '0', totalSaved: '0' },
    ])
    Voucher.bulkCreate([
      { uuid: 'a8e66bcc-790b-4d06-91b1-f49b45de6910', discount_number: 15, used: false, user_uuid: 'ad5053d3-7a11-4de2-ba25-1dfa7768efc2' },
    ])
  });

module.exports = {
  User,
  Transaction,
  Voucher,
  sequelize,
}