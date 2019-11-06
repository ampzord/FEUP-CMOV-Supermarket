module.exports = (sequelize, type) => {
  return sequelize.define('user', {
    id: { type: type.INTEGER, primaryKey: true, autoIncrement: true },
    fName: { type: type.STRING, allowNull: false },
    lName: { type: type.STRING, allowNull: false },
    username: { type: type.STRING, allowNull: false, unique: true },
    password: { type: type.STRING, allowNull: false },
    password_conf: { type: type.STRING, allowNull: false },
    credit_card: { type: type.BIGINT, allowNull: false },
    public_key: { type: type.STRING, allowNull: false },
    uuid: { type: type.STRING, allowNull: false, unique: true  },
  })
}