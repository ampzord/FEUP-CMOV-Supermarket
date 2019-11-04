package fe.up.pt.supermarket.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Enumeration;

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
        Log.d(KEY_TAG, "Public key: " + publicKey.toString());
        Log.d(KEY_TAG, "Private key: " + privateKey.toString());


        byte[] pKbytes = Base64.encode(publicKey.getEncoded(), 0);
        String pK = new String(pKbytes);
        String pubKeyFormmated = "-----BEGIN PUBLIC KEY-----\n" + pK + "-----END PUBLIC KEY-----\n";

        Log.d(KEY_TAG, "Public key FORMATTED: " + pubKeyFormmated);
        return pubKeyFormmated;
    }


    public void getAllKeyStoreKeys() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
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
}
