module.exports = (sequelize, type) => {
    return sequelize.define('voucher', {
        id: { type: type.INTEGER, primaryKey: true, autoIncrement: true },
        uuid: { type: type.STRING, allowNull: false },
        discount_number: { type: type.INTEGER, allowNull: false },
        used: { type: type.BOOLEAN, allowNull: false },
        user_uuid: { type: type.STRING, allowNull: false },
    })
}