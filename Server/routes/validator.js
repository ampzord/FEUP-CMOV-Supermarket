const { check, validationResult } = require('express-validator');

module.exports = {
    validateRegister: [
        check('username')
            .isLength({ min:1 }).withMessage('Username is a required field.')
            .isAlphanumeric().withMessage('Username must be alphanumeric.'),

        check('password')
            .isLength({ min:8 }).withMessage('Password must be at least 6 characters in length.')
            .custom((value, {req, loc, path}) => {
                if (value !== req.body.password_conf) {
                    return false;
                } else {
                    return value;
                }
            }).withMessage("Passwords don't match."),
    ],
    errorFormatter: ({location, msg, param, value, nestedErrors}) => {
            return {
                type: "Error",
                name: "Signup Failure",
                location: location,
                message: msg,
                param: param,
                value: value,
                nestedErrors: nestedErrors,
            }
    }
}