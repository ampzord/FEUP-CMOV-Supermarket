package fe.up.pt.supermarket.utils;

import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
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
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

import fe.up.pt.supermarket.activities.LoginActivity;
import fe.up.pt.supermarket.activities.RegistrationActivity;

import static android.content.ContentValues.TAG;

public class KeyStoreUtils {

    private static String KEY_TAG = "KEY_TAG";

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
                LoginActivity.SERVER_CERTIFICATE = cert.getPublicKey();
                LoginActivity.hasServerKey = true;
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
            LoginActivity.SERVER_CERTIFICATE = x509.getPublicKey();
            LoginActivity.hasServerKey = true;
        }
        catch(Exception e) {
            Log.d(KEY_TAG, "Error creating certificate.");
            e.printStackTrace();
        }
    }

    public static String getUserPublicKeyCertificate(String username) {
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(username, null);
            if (entry != null) {
                X509Certificate cert = (X509Certificate)((KeyStore.PrivateKeyEntry)entry).getCertificate();
                //Log.d("CERTIFICATE", "Certificate for Server: " + cert.getEncoded());
                byte[] encCert = cert.getEncoded();
                String strCert = cert.toString();
                String b64Cert = Base64.encodeToString(encCert, Base64.DEFAULT);
                String text = "cert(b64): " + b64Cert + "\n\n" + strCert;
                //Log.d("CERTIFICATE", "Certificate for ServerB64: " + b64Cert);
                text = "-----BEGIN CERTIFICATE-----\n" + b64Cert +
                        "-----END CERTIFICATE-----\n";
                Log.d("CERTIFICATE", text);
                return text;
            }
        }
        catch (Exception e) {
            Log.d("CERTIFICATE", e.getMessage());
            return "error";
        }
        return "";
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
