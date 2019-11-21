module.exports = (sequelize, type) => {
    return sequelize.define('voucher', {
        id: { type: type.INTEGER, primaryKey: true, autoIncrement: true },
        uuid: { type: type.STRING, allowNull: false, unique: true  },
        discount_number: { type: type.INTEGER, allowNull: false },
        used: { type: type.BOOLEAN, allowNull: false },
    })
}