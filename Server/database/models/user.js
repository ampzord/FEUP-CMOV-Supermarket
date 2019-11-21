module.exports = (sequelize, type) => {
  return sequelize.define('user', {
    id: { type: type.INTEGER, primaryKey: true, autoIncrement: true },
    fName: { type: type.STRING, allowNull: true },
    lName: { type: type.STRING, allowNull: true },
    username: { type: type.STRING, allowNull: true, unique: true },
    password: { type: type.STRING, allowNull: true },
    public_key: { type: type.STRING, allowNull: true },
    uuid: { type: type.STRING, allowNull: true  },
    credit_card_number: { type: type.BIGINT, allowNull: true },
    credit_card_name: { type: type.STRING, allowNull: true },
    credit_card_exp_date: { type: type.STRING, allowNull: true },
    credit_card_cvc: { type: type.STRING, allowNull: true },
    totalSpent: { type: type.DOUBLE, allowNull: true },
    totalSaved: { type: type.INTEGER, allowNull: true },
  })
}