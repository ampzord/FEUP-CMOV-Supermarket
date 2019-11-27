module.exports = (sequelize, type) => {
    return sequelize.define('transaction', {
        id: { type: type.INTEGER, primaryKey: true, autoIncrement: true },
        uuid: { type: type.STRING, allowNull: false },
        user_uuid: { type: type.STRING, allowNull: false },
        voucher_uuid: { type: type.STRING, allowNull: true },
        price: { type: type.DOUBLE, allowNull: false },
        products_size: { type: type.INTEGER, allowNull: false },
        discount_used: { type: type.BOOLEAN, allowNull: false },
    })
}