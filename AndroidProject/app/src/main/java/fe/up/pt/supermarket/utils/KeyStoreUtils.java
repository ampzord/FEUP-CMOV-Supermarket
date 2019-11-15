package fe.up.pt.supermarket.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import fe.up.pt.supermarket.activities.RegistrationActivity;

import static android.content.ContentValues.TAG;

public class KeyStoreUtils {

    private static String KEY_TAG = "KEY_TAG";

    public static String generateKeys(String username) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        /*
         * Generate a new EC key pair entry in the Android Keystore by
         * using the KeyPairGenerator API. The private key can only be
         * used for signing or verification and only with SHA-256 or
         * SHA-512 as the message digest.
         */
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
        kpg.initialize(new KeyGenParameterSpec.Builder(
                username,
                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                .setDigests(KeyProperties.DIGEST_SHA256,
                        KeyProperties.DIGEST_SHA512)
                .build());

        KeyPair kp = kpg.generateKeyPair();
        PublicKey publicKey = kp.getPublic();
        PrivateKey privateKey = kp.getPrivate();
        //Log.d(KEY_TAG, "Public key: " + publicKey.toString());
        //Log.d(KEY_TAG, "Private key: " + privateKey.toString());


        byte[] pKbytes = Base64.encode(publicKey.getEncoded(), Base64.DEFAULT);
        String pK = new String(pKbytes);
        String pubKeyFormated = "-----BEGIN PUBLIC KEY-----\n" + pK + "-----END PUBLIC KEY-----\n";
        //Log.d(KEY_TAG, "Public key FORMATTED: " + pubKeyFormated);

        String encodedPubKey = new String(Base64.encode(pubKeyFormated.getBytes(), Base64.DEFAULT));
        //Log.d(KEY_TAG, "Public key ENCODED: " + encodedPubKey);

        /*byte[] privKeyBytes = Base64.encode(privateKey.getEncoded(), Base64.DEFAULT);
        String privK = new String(privKeyBytes);
        String privKeyFormated = "-----BEGIN PRIVATE KEY-----\n" + privK + "-----END PRIVATE KEY-----\n";
        Log.d(KEY_TAG, "Private key FORMATTED: " + privKeyFormated);*/

        return encodedPubKey;
    }

    public static void getAllKeyStoreKeys() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        /*
         * Load the Android KeyStore instance using the
         * "AndroidKeyStore" provider to list out what entries are
         * currently stored.
         */
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        Enumeration<String> aliases = ks.aliases();

        while (aliases.hasMoreElements()) {
            Log.d(KEY_TAG, "Keys: " + aliases.nextElement());
        }
    }

    public static void getServerKeyFromKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            keyStore.load(null);
            Certificate cert = keyStore.getCertificate(Constants.keyAlias);
            if (cert != null) {
                RegistrationActivity.pub = cert.getPublicKey();
                RegistrationActivity.hasServerKey = true;
            }
        }
        catch(Exception e) {
            Log.d(KEY_TAG, "Error verifying if server key exists.");
        }
    }

    public static void getCertificateFromServerMessage(JSONObject str) {
        try {
            String s_pub_key = str.getString("server_public_key");
            //Log.d(TAG_REGISTER, "s_cert: " + s_pub_key);
            byte[] cert = s_pub_key.getBytes(StandardCharsets.UTF_8);
            KeyStore keyStore = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            keyStore.load(null);
            X509Certificate x509 = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(cert));
            keyStore.setEntry(Constants.keyAlias, new KeyStore.TrustedCertificateEntry(x509), null);
            RegistrationActivity.pub = x509.getPublicKey();
            RegistrationActivity.hasServerKey = true;
        }
        catch(Exception e) {
            Log.d(KEY_TAG, "Error creating certificate.");
            e.printStackTrace();
        }
    }

    public static boolean deleteKey(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry(alias);
            return true;
        } catch (KeyStoreException | CertificateException | IOException
                | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] signData(String alias, byte[] data)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
            InvalidKeyException, UnrecoverableEntryException, SignatureException {
        /*
         * Use a PrivateKey in the KeyStore to create a signature over
         * some data.
         */
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(alias, null);
        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            Log.w(KEY_TAG, "Not an instance of a PrivateKeyEntry");
            return null;
        }
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
        s.update(data);
        byte[] signature = s.sign();
        return signature;
    }

    //TODO server_public_key
    public static boolean verifyData(String alias, byte[] data)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
            InvalidKeyException, SignatureException, UnrecoverableEntryException {
        /*
         * Verify a signature previously made by a PrivateKey in our
         * KeyStore. This uses the X.509 certificate attached to our
         * private key in the KeyStore to validate a previously
         * generated signature.
         */
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(alias, null);
        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            Log.w(TAG, "Not an instance of a PrivateKeyEntry");
            return false;
        }
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initVerify(((KeyStore.PrivateKeyEntry) entry).getCertificate());
        s.update(data);
        //boolean valid = s.verify(signature);
        return false;
    }





}
