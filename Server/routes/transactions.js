const Joi = require('joi');
const express = require('express');
const router = express.Router();
const { check, validationResult } = require('express-validator')
const { User } = require('../database')
const { Transaction } = require('../database')
const { Voucher } = require('../database')

/* Perform a transaction */
router.post('/transaction', function(req, res, next) {
    console.log(req.body);

    // when the server receives a voucher in a payment he should
    // check if it belongs to the right user AND was not used.

    var voucher_;
    var hasVoucher = false;
    var finalPrice;
    var newTotalSaved;

    var foundVoucher;

    /*if (req.body.voucher_uuid != null) {
        hasVoucher = true;
        foundVoucher = Voucher.findOne({
            where: {uuid: req.body.voucher_uuid}
        });

        if (foundVoucher.length === 0) {
          res.status(500).json('This voucher doesn\'t exist.');
        }

        if (foundVoucher.used !== false) {
          res.status(500).json('This voucher has already been used.');
        }

        if (foundVoucher.user_uuid !== req.body.user_uuid) {
          res.status(500).json('This voucher doesn\'t belong to this user.');
        }

        foundVoucher.update({
            used: true,
        })
    }*/


    const foundUser = User.findOne({
        where: {uuid: req.body.user_uuid}
    });

    console.log(foundUser);

    if (req.body.discount) {
        if (foundUser.totalSaved > finalPrice) {
          newTotalSaved = foundUser.totalSaved - finalPrice;
          finalPrice = 0;
        }
        else {
          finalPrice = finalPrice - foundUser.totalSaved;
          newTotalSaved = 0;
        }
    }

    if (hasVoucher) {
        var discount_numberToAdd = (foundVoucher.discount_number * finalPrice) / 100;
    }

   var newTotalSaved_ = foundUser.totalSaved + discount_numberToAdd;
   var newTotalSpent_ = foundUser.totalSpent + finalPrice;

   foundUser.update({
        totalSaved: newTotalSaved_,
        totalSpent: newTotalSpent_,
   });

    //generateVoucher
    newVoucherGenerator(finalPrice, req.body.user_uuid);

    //var uuid_gen_transaction = Uuid.fromString(req.body.uuid);

    //parse transaction

    if (hasVoucher) {
        Transaction.create({
            uuid: req.body.uuid,
            user_uuid: req.body.user_uuid,
            voucher_uuid: req.body.voucher_uuid,
            price: req.body.price,
            discount: req.body.discount,
            products_size: req.body.products_size,
        }).then(transaction => {

            res.status(200).json({
                ok: true,
                message: 'OPEN TERMINAL DOORS',
            })
        }).catch(err => {
            console.log(err.errors[0].message);
            res.status(500).json('Error generating transaction');
        });
    }
    else {
        Transaction.create({
            uuid: req.body.uuid,
            user_uuid: req.body.user_uuid,
            price: req.body.price,
            discount: req.body.discount,
            products_size: req.body.products_size,
        }).then(transaction => {

            res.status(200).json({
                ok: true,
                message: 'OPEN TERMINAL DOORS',
            })
        }).catch(err => {
            console.log(err.errors[0].message);
            res.status(500).json('Error generating transaction');
        });
    }
});

function newVoucherGenerator(price, uuid) {
    var discount;

    var result = price / 100;

    if (result < 1) {
        discount = 15;
    } else if (result < 2) {
        discount = 30;
    }
    else if (result < 3) {
        discount = 45;
    }
    else if (result < 4) {
        discount = 60;
    }

    Voucher.create({
        uuid: uuidv4(),
        discount_number: discount,
        used: false,
        user_uuid: uuid,
    });
}

/** All transactions of a user of a given UUID */
router.get('/transactions/:uuid', function(req, res, next) {
    console.log(req.body);

    Transaction.findAll({
        where: {
            user_uuid: req.params.uuid
        }
    }).then(transactions => res.json(transactions));
});

/** All vouchers of a user of a given UUID */
router.get('/vouchers/:uuid', function(req, res, next) {
    console.log(req.body);

    Voucher.findAll({
        where: {
            user_uuid: req.params.uuid,
            used: false
        }
    }).then(vouchers => res.json(vouchers));
});

module.exports = router;