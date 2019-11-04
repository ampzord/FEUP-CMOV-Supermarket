var crypto = require("crypto");
var path = require("path");
const { writeFileSync, fs } = require("fs");
const { generateKeyPair } = require('crypto');
//const NodeRSA = require('node-rsa');

function generateKeys() {
  generateKeyPair('rsa', {
    modulusLength: 4096,
    publicKeyEncoding: {
      type: 'spki',
      format: 'pem'
    },
    privateKeyEncoding: {
      type: 'pkcs8',
      format: 'pem',
      cipher: 'aes-256-cbc',
      passphrase: 'secretpassword'
    }
  }, (err, publicKey, privateKey) => {
    // Handle errors and use the generated key pair.
    if (err) {
      console.log('Error while generating private and public key of Server.')
    } else {
      writeFileSync('/keys/server_private_key.pem', privateKey);
      writeFileSync('/keys/server_public_key.pem', publicKey);
      return publicKey;
    }

  });
}

function encrypt(toEncrypt, relativeOrAbsolutePathToPublicKey) {
  const absolutePath = path.resolve(relativeOrAbsolutePathToPublicKey)
  const publicKey = fs.readFileSync(absolutePath, 'utf8')
  const buffer = Buffer.from(toEncrypt, 'utf8')
  const encrypted = crypto.publicEncrypt(publicKey, buffer)
  return encrypted.toString('base64')
}

function decrypt(toDecrypt, relativeOrAbsolutePathtoPrivateKey) {
  const absolutePath = path.resolve(relativeOrAbsolutePathtoPrivateKey)
  const privateKey = fs.readFileSync(absolutePath, 'utf8')
  const buffer = Buffer.from(toDecrypt, 'base64')
  const decrypted = crypto.privateDecrypt(
    {
      key: privateKey.toString(),
      passphrase: '',
    },
    buffer,
  )
  return decrypted.toString('utf8')
}

/*
const enc = encrypt('hello', `public.pem`)
console.log('enc', enc)

const dec = decrypt(enc, `private.pem`)
console.log('dec', dec)
*/

module.exports = {
    encryptStringWithRsaPublicKey: encryptStringWithRsaPublicKey,
    decryptStringWithRsaPrivateKey: decryptStringWithRsaPrivateKey,
    generateKeys
}