module.exports = (sequelize, type) => {
  return sequelize.define('user', {
    id: { type: type.INTEGER, primaryKey: true, autoIncrement: true },
    name: { type: type.STRING, allowNull: false },
    username: { type: type.STRING, allowNull: false, unique: true },
    password: { type: type.STRING, allowNull: false },
    credit_card: { type: type.BIGINT, allowNull: false},
  })
}