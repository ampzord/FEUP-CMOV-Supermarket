const crypto = require("crypto");
const path = require("path");
const { writeFileSync, fs } = require("fs");
const { generateKeyPairSync } = require('crypto');

const server_private_key = `-----BEGIN RSA PRIVATE KEY-----
MIIBOQIBAAJBALAMF4uG90wza84n4eP0bUsgwqMMt77yZ7kCNxZPvT0ZS9EUOaeb
g9m/YIl7OQHoClO4pinWf9AvGTGSp6jFW/ECAwEAAQJAbFBkRCJcEPQqRgLfxqLi
g7C6UBtJCiCWlIu6XO3enrHkpqLgTQnN6OM0YfFNBnQS63Jr8kW2fTp1ut3fnPHj
6QIhAO5xk1ygQhp3jXiJQXuIX/CTSvf3Cg9JzWlr+wj6FOebAiEAvQJn/sRrKDoT
WOo8KMXGXJ8JlnL88LP8amu7ifETkWMCIEdsb/bL7mEdNJxJiPrm884NYOR79GB1
+/i7JpwexQpHAiBI78bFz3Iygca5hxpT+Y4Ea/K9Nf5ZvHrpbk1ee8M7VQIgbVsE
sjY95b/yercsjfBGMRa1goiapzm0gGjoQTTyEzI=
-----END RSA PRIVATE KEY-----`;

const server_public_key = `-----BEGIN PUBLIC KEY-----
MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALAMF4uG90wza84n4eP0bUsgwqMMt77y
Z7kCNxZPvT0ZS9EUOaebg9m/YIl7OQHoClO4pinWf9AvGTGSp6jFW/ECAwEAAQ==
-----END PUBLIC KEY-----`;

const server_certificate = `-----BEGIN CERTIFICATE-----
MIIBGTCBxKADAgECAgQAuPR8MA0GCSqGSIb3DQEBCwUAMBQxEjAQBgNVBAMTCVNlcnZlcktleTAe
Fw0xOTExMTUwMDUzMDhaFw0zOTExMTUwMDUzMDhaMBQxEjAQBgNVBAMTCVNlcnZlcktleTBcMA0G
CSqGSIb3DQEBAQUAA0sAMEgCQQCijdPkGMWAqFq+pL/nCnowaFxJZMxe4xRYlPjp5Rv8zM6eG4NQ
RRzhIIqzPkyEs4R+MNPPUKQcWzNTNRR0d6ArAgMBAAEwDQYJKoZIhvcNAQELBQADQQChOANsJ548
x+1HBh+bXz8J3DuxnKDLQrwEGJKou0Ks/JMUXOyNjnFkl4DAmj6nV/eC/PlCiaCJQKy4CWTp+iH1
-----END CERTIFICATE-----`;

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

//encripta com public key
function encrypt(toEncrypt, relativeOrAbsolutePathToPublicKey) {
  const absolutePath = path.resolve(relativeOrAbsolutePathToPublicKey)
  const publicKey = fs.readFileSync(absolutePath, 'utf8')
  const buffer = Buffer.from(toEncrypt, 'utf8')
  const encrypted = crypto.publicDecrypt(publicKey, buffer)
  return encrypted.toString('base64')
}

//decrypt com private key
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
  server_public_key,
    server_certificate,
};