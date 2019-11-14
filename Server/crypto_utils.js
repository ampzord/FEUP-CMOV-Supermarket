const crypto = require("crypto");
const path = require("path");
const { writeFileSync, fs } = require("fs");
const { generateKeyPairSync } = require('crypto');

const server_private_key = `-----BEGIN PRIVATE KEY-----
MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAwzTF8SZuWwUCPlZN
pTytoD+GH5aQj7N6UYgHZlXHErqaWJvnajrL7e0k9FpdpuOYfYAP2w+ResKuMkIi
gHnxKQIDAQABAkBcfkvwOMJ/dD8c5G3EBp1KWe8mVoRG4sbpjOfcsHY0Q8zag+hW
w8+YVa+5WDjYL3Z9F0Rl0WOENi5Xc3hiId0BAiEA5weGX+fCFMN8x1pahcaKpr57
pxWc1qgYAuoEiZ/NHgkCIQDYTgkqIgYrud+Z3V6bLDKas6aa99ZKGSFgr391qaUC
IQIhAITZiOXhaXNzLm+cf21pzBUyd/yOqw+svZH/a/iP0e2xAiEAmTqer2Qu7ubb
iYoSLOagaor9aSZMfW1UAcQRDO9CX0ECIDU21TL/mABbUajV9viEb86YpbPbYdbO
DpTgNGb9GJFY
-----END PRIVATE KEY-----`;

const server_public_key = `-----BEGIN PUBLIC KEY-----
MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMM0xfEmblsFAj5WTaU8raA/hh+WkI+z
elGIB2ZVxxK6mlib52o6y+3tJPRaXabjmH2AD9sPkXrCrjJCIoB58SkCAwEAAQ==
-----END PUBLIC KEY-----`;

function generateKeys() {
  const { privateKey, publicKey } = generateKeyPairSync('rsa', {
      modulusLength: 512,
      publicKeyEncoding: {
        type: 'spki',
        format: 'pem',
      },
      privateKeyEncoding: {
        type: 'pkcs8',
        format: 'pem',
      },
  });
  writeFileSync(__dirname + '/keys/server_private_key.pem', privateKey);
  writeFileSync(__dirname + '/keys/server_public_key.pem', publicKey);
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
  encrypt,
  decrypt,
  generateKeys,
  server_private_key,
  server_public_key
}