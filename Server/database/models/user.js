module.exports = (sequelize, type) => {
  return sequelize.define('user', {
    id: { type: type.INTEGER, primaryKey: true, autoIncrement: true },
    fName: { type: type.STRING, allowNull: false },
    lName: { type: type.STRING, allowNull: false },
    username: { type: type.STRING, allowNull: false, unique: true },
    password: { type: type.STRING, allowNull: false },
    public_key: { type: type.STRING, allowNull: false },
    uuid: { type: type.STRING, allowNull: false, unique: true  },
    credit_card_number: { type: type.BIGINT, allowNull: false },
    credit_card_name: { type: type.STRING, allowNull: false },
    credit_card_exp_date: { type: type.STRING, allowNull: false },
    credit_card_cvc: { type: type.STRING, allowNull: false },
    totalSpent: { type: type.DOUBLE, allowNull: false },
  })
}