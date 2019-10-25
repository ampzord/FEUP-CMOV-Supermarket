const Sequelize = require('sequelize')
const UserModel = require('./models/user')

const sequelize = new Sequelize('acme', 'admin', 'sqladmin', {
  host: 'localhost',
  dialect: 'sqlite',
  operatorsAliases: false,
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
    console.log(`Database & tables created!`)
    User.bulkCreate([
      { name: 'Ricardo Fonseca', username: 'rfonseca', password: 'password1', password_conf: 'password1', credit_card: '4611441749020896' },
      { name: 'Miguel Moreira', username: 'mmoreira', password: 'password2', password_conf: 'password2', credit_card: '4025668462246072' },
    ])
  })

module.exports = {
  User,
  sequelize
}