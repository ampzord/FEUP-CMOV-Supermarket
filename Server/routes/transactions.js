const Joi = require('joi');
const express = require('express');
const router = express.Router();
const { check, validationResult } = require('express-validator');
const { User } = require('../database/');
const { Transaction } = require('../database');
const { Voucher } = require('../database');
const uuidv4 = require('uuid/v4');

/* Perform a transaction */
router.post('/transaction', function(req, res, next) {

    // when the server receives a voucher in a payment he should
    // check if it belongs to the right user AND was not used.

    console.log(req.body);

    const price_number = +req.body.price * 1;

    let hasVoucher = false;
    let newFinalPrice = req.body.price;
    let newTotalSaved = 0;
    let voucherDiscountCode;
    let username_temp = req.body.user_uuid;

    User.findOne({
        where : { uuid : req.body.user_uuid }
    }).then( user => {
        if (user) {
            //console.log(user.dataValues);

            username_temp = user.username;

            let newTotalSpent_;
            let newTotalSaved_;
            let numberToSavings;

            if (req.body.discount_used == 1) {
                if (user.totalSaved > price_number) {
                    newTotalSaved = +user.totalSaved - +price_number;
                    newFinalPrice = 0;
                }
                else {
                    newFinalPrice = +newFinalPrice - +user.totalSaved;
                    newTotalSaved = 0;
                }
            }

            newTotalSaved_ = newTotalSaved;
            newTotalSpent_ = +user.totalSpent + +newFinalPrice;

            if (hasVoucher) {
                numberToSavings = (voucherDiscountCode * newFinalPrice) / 100;
                newTotalSaved_ = +newTotalSaved + +numberToSavings;
            }

            user.update(
                {
                    totalSpent: newTotalSpent_,
                    totalSaved: newTotalSaved_,
                }
            );

            //console.log("user after updating values");
            //console.log(user.dataValues);
        }
    }).catch(err => {
        console.log("Error calculating new Price.");
        console.log(err);
    });

    var discount = newVoucherGeneratorDiscount(newFinalPrice, req.body.user_uuid);

    if (newFinalPrice > 100) {
        Voucher.create({
            uuid: uuidv4(),
            discount_number: discount,
            used: false,
            user_uuid: req.body.user_uuid,
        }).then( voucher => {
            console.log("Created voucher successfully: ");
            console.log(voucher.dataValues);
        }).catch(err => {
            console.log("Error creating voucher.");
        });
    }

    if (req.body.voucher_uuid) {
        Voucher.findOne({
            where : { uuid : req.body.voucher_uuid }
        }).then( (voucher) => {
            if (voucher) {
                //console.log(voucher.dataValues);

                if (voucher.user_uuid != req.body.user_uuid) {
                    console.log("This voucher doesn't belong to this user.");
                    res.status(500).json('This voucher doesn\'t belong to this user.');
                }

                if (voucher.used) {
                    console.log("This voucher has already been used.");
                    res.status(500).json('This voucher has already been used.');
                }

                voucherDiscountCode = voucher.discount_number;

                voucher.update({
                    used : true,
                });
                
                //console.log("After updating voucher.");
                //console.log(voucher.dataValues);
            }
        });

        Transaction.create({
            uuid: req.body.uuid,
            user_uuid: req.body.user_uuid,
            voucher_uuid: req.body.voucher_uuid,
            price: price_number,
            discount_used: req.body.discount_used,
            products_size: req.body.products_size,
        }).then(transaction => {
            console.log("Transaction created for user: " + username_temp);
            console.log(transaction.dataValues);
            res.status(200).json({
                ok: true,
                message: 'OPEN TERMINAL DOORS',
            })
        }).catch(err => {
            //console.log(err.errors[0].message);
            res.status(500).json('Error generating transaction');
        });
    }

    else {
        Transaction.create({
            uuid: req.body.uuid,
            user_uuid: req.body.user_uuid,
            price: req.body.price,
            discount_used: req.body.discount_used,
            products_size: req.body.products_size,
        }).then(transaction => {
            console.log("Transaction created for user: " + username_temp);
            console.log(transaction.dataValues);
            res.status(200).json({
                ok: true,
                message: 'OPEN TERMINAL DOORS',
            })
        }).catch(err => {
            console.log(err);
            res.status(500).json('Error generating transaction');
        });
    }
});

function newVoucherGeneratorDiscount(price) {
    var discount;
    var result = price / 100;

    if (result < 2) {
        discount = 15;
    } else if (result < 3) {
        discount = 30;
    }
    else if (result < 4) {
        discount = 45;
    }
    else {
        discount = 60;
    }

    return discount;
}

/** All transactions of a user of a given UUID */
router.get('/transactions/:uuid', function(req, res, next) {
    Transaction.findAll({
        where: {
            user_uuid: req.params.uuid,
        }
    }).then(transactions => {
        res.status(200).json({
            transactions: transactions,
        })
    });
});

/** All vouchers of a user of a given UUID */
router.get('/vouchers/:uuid', function(req, res, next) {
    Voucher.findAll({
        where: {
            user_uuid: req.params.uuid,
        }
    }).then(vouchers => {
        res.status(200).json({
            vouchers: vouchers,
        })
    });
});


/* GET all transactions listing. */
router.get('/transactions', function(req, res, next) {
    Transaction.findAll({ raw: true }).then(transaction => res.json(transaction))
});

/* GET all vouchers listing. */
router.get('/vouchers', function(req, res, next) {
    Voucher.findAll().then(voucher => res.json(voucher))
});

module.exports = router;