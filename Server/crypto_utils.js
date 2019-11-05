const crypto = require("crypto");
const path = require("path");
const { writeFileSync, fs } = require("fs");
const { generateKeyPairSync } = require('crypto');

const server_private_key = `-----BEGIN RSA PRIVATE KEY-----
MIIBOgIBAAJBAMUfX/Pj+RMmLGGk7UXp9J54i7FTXueOR+h9Y9FXpiDi37KSqTil
jd8Kf+RHx+Vj8Yazeg+aQCCmONk9akjrzv8CAwEAAQJAaIVwXWPeKBcvpT7MSSv6
dyS3/XiVc/ZvjokeKlxtTDXRPe2zeGtGljdazBVSp3qPuqOOJZpZjcch1NfiOqQQ
IQIhAPIbb1+8NbLfAksD/ouJvjGgqxnLSs07jXL9gM1tns/nAiEA0G8dJcF1mCCJ
CBMhr04oNWgX5lI+sBzrCsD7dZKLBSkCIAZ/hAK+y3YslCQtTES0gr1UQaNkmHJf
udEvSqi4231bAiAr5isiZ5OH3dpenADtNi3bybe2572SRBTw5+JOSfYDuQIhAJ9V
s6JR7AiSsjj83z9knxd0q/iLTTPtZB0exxIS7x3o
-----END RSA PRIVATE KEY-----`;

const server_public_key = `-----BEGIN RSA PUBLIC KEY-----
MEgCQQDFH1/z4/kTJixhpO1F6fSeeIuxU17njkfofWPRV6Yg4t+ykqk4pY3fCn/k
R8flY/GGs3oPmkAgpjjZPWpI687/AgMBAAE=
-----END RSA PUBLIC KEY-----`;

function generateKeys() {
  const { privateKey, publicKey } = generateKeyPairSync('rsa', {
      modulusLength: 512,
      publicKeyEncoding: {
        type: 'pkcs1',
        format: 'pem',
      },
      privateKeyEncoding: {
        type: 'pkcs1',
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