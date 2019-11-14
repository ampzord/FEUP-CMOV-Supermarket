package org.feup.apm.genproducttag;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.util.Base64;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

public class MainActivity extends AppCompatActivity {
  final String TAG = "GenProductTag";
  UUID productUUID;
  boolean hasKey = false;
  PrivateKey pri;
  PublicKey pub;
  byte[] encTag;

  EditText edUUID, edName, edEuros, edCents;
  TextView tvNoKey, tvKey;

  private final String sv_private_key = "-----BEGIN PRIVATE KEY-----\n" +
          "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAwzTF8SZuWwUCPlZN\n" +
          "pTytoD+GH5aQj7N6UYgHZlXHErqaWJvnajrL7e0k9FpdpuOYfYAP2w+ResKuMkIi\n" +
          "gHnxKQIDAQABAkBcfkvwOMJ/dD8c5G3EBp1KWe8mVoRG4sbpjOfcsHY0Q8zag+hW\n" +
          "w8+YVa+5WDjYL3Z9F0Rl0WOENi5Xc3hiId0BAiEA5weGX+fCFMN8x1pahcaKpr57\n" +
          "pxWc1qgYAuoEiZ/NHgkCIQDYTgkqIgYrud+Z3V6bLDKas6aa99ZKGSFgr391qaUC\n" +
          "IQIhAITZiOXhaXNzLm+cf21pzBUyd/yOqw+svZH/a/iP0e2xAiEAmTqer2Qu7ubb\n" +
          "iYoSLOagaor9aSZMfW1UAcQRDO9CX0ECIDU21TL/mABbUajV9viEb86YpbPbYdbO\n" +
          "DpTgNGb9GJFY\n" +
          "-----END PRIVATE KEY-----";

  private final String sv_public_key = "-----BEGIN PUBLIC KEY-----\n" +
          "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMM0xfEmblsFAj5WTaU8raA/hh+WkI+z\n" +
          "elGIB2ZVxxK6mlib52o6y+3tJPRaXabjmH2AD9sPkXrCrjJCIoB58SkCAwEAAQ==\n" +
          "-----END PUBLIC KEY-----";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    edUUID = findViewById(R.id.ed_uuid);
    edName = findViewById((R.id.ed_name));
    edEuros = findViewById(R.id.ed_euros);
    edCents = findViewById((R.id.ed_cents));
    tvNoKey = findViewById(R.id.tv_nokey);
    tvKey = findViewById(R.id.tv_pubkey);
    findViewById(R.id.bt_ugen).setOnClickListener((v)->{
      productUUID =  UUID.randomUUID();
      edUUID.setText(productUUID.toString());
    });
    findViewById(R.id.bt_gentag).setOnClickListener((v)->genTag());
    findViewById(R.id.bt_dectag).setOnClickListener((v)->decTag());
    findViewById(R.id.bt_genkey).setOnClickListener((v)->genKeys());
    findViewById(R.id.bt_send).setOnClickListener((v)->sendKey());
    findViewById(R.id.bt_showkey).setOnClickListener((v)->showKey());

    /*try {
      KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
      ks.load(null);
      KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
      hasKey = (entry != null);
      if (hasKey) {
        pub = ((KeyStore.PrivateKeyEntry)entry).getCertificate().getPublicKey();
        pri = ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();
        tvNoKey.setText("");
      }
    }
    catch (Exception e) {
    }*/
    //Log.d("TAG_KEY", "Value of hasKey: " + hasKey);

    try {
      pub = getPublicKeyFromString(sv_public_key);
      String newPublicKey = publicKeyToString(pub);
      Log.d("TAG_KEY", "newPublicKey: " + newPublicKey);

      pri = getPrivateKeyFromString(sv_private_key);
      Log.d("TAG_KEY", "newPrivateKey: " + pri.toString());

    } catch (Exception e) {
      Log.d("TAG_KEY", "Caught Exception");
      e.printStackTrace();
    }

    Log.d("TAG_KEY", "PASSOU");

    hasKey = true;
    tvNoKey.setText("");

    findViewById(R.id.bt_send).setEnabled(hasKey);
    findViewById(R.id.bt_showkey).setEnabled(hasKey);
    findViewById(R.id.bt_genkey).setEnabled(!hasKey);
  }

  void genKeysServer() {
    String text;

    try {
      Calendar start = new GregorianCalendar();
      Calendar end = new GregorianCalendar();
      end.add(Calendar.YEAR, 20);
      KeyPairGenerator kgen = KeyPairGenerator.getInstance(Constants.KEY_ALGO, Constants.ANDROID_KEYSTORE);
      AlgorithmParameterSpec spec = new KeyPairGeneratorSpec.Builder(this)
              .setKeySize(Constants.KEY_SIZE)
              .setAlias(Constants.keyname)
              .setSubject(new X500Principal("CN=" + Constants.keyname))
              .setSerialNumber(BigInteger.valueOf(Constants.CERT_SERIAL))
              .setStartDate(start.getTime())
              .setEndDate(end.getTime())
              .build();
      kgen.initialize(spec);
      KeyPair kp = kgen.generateKeyPair();
      pri = kp.getPrivate();                                         // private key in a Java class (PrivateKey)
      pub = kp.getPublic();                                          // the corresponding public key in a Java class (PublicKey)
      hasKey = true;
    }
    catch (Exception e) {
      tvNoKey.setText(e.getMessage());
      return;
    }
    findViewById(R.id.bt_send).setEnabled(hasKey);
    findViewById(R.id.bt_showkey).setEnabled(hasKey);
    findViewById(R.id.bt_genkey).setEnabled(!hasKey);
    tvNoKey.setText("");
    byte[] modulus = ((RSAPublicKey)pub).getModulus().toByteArray();
    text = "Modulus (" + modulus.length + "):\n";
    text += byteArrayToHex(modulus) + "\n";
    text += "Public Exponent:\n";
    text += byteArrayToHex(((RSAPublicKey)pub).getPublicExponent().toByteArray()) + "\n";
    tvKey.setText(text);
  }

  public static String publicKeyToString(PublicKey publ) throws GeneralSecurityException {
    KeyFactory fact = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec spec = fact.getKeySpec(publ,
            X509EncodedKeySpec.class);
    return Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);
  }

  public PublicKey getPublicKeyFromString(String keystr) throws Exception {
    // Remove the first and last lines
    //PEM FILE STARTS WITH BEGIN RSA PUBLIC KEY
    String pubKeyPEM = keystr.replace("-----BEGIN PUBLIC KEY-----\n", "");
    pubKeyPEM = pubKeyPEM.replace("-----END PUBLIC KEY-----", "");

    // Base64 decode the data

    byte [] encoded = Base64.decode(pubKeyPEM, Base64.DEFAULT);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    PublicKey pubkey = kf.generatePublic(keySpec);

    return pubkey;
  }

  public PrivateKey getPrivateKeyFromString(String str) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
    String privKeyPEM = str.replace("-----BEGIN PRIVATE KEY-----\n", "");
    privKeyPEM = privKeyPEM.replace("-----END PRIVATE KEY-----", "");

    byte[] encoded = Base64.decode(privKeyPEM, Base64.DEFAULT);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    PrivateKey sessionkey = kf.generatePrivate(keySpec);
    return sessionkey;
  }

  public String RSAEncrypt(final String plain) throws NoSuchAlgorithmException, NoSuchPaddingException,
          InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {

    /*if (pubKey != null) {
      cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, pubKey);
      encryptedBytes = cipher.doFinal(plain.getBytes());
      Log.d("BYTES", new String(encryptedBytes));
      //return Hex.encodeHexString(encryptedBytes);
      return new String(Base64.encode(encryptedBytes));
    }
    else*/
      return null;
  }

  public void generateKeys(String username) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
    /*
     * Generate a new EC key pair entry in the Android Keystore by
     * using the KeyPairGenerator API. The private key can only be
     * used for signing or verification and only with SHA-256 or
     * SHA-512 as the message digest.
     */
    KeyPairGenerator kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      kpg.initialize(new KeyGenParameterSpec.Builder(
              "SERVER_PUBLIC_KEY",
              KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
              .setDigests(KeyProperties.DIGEST_SHA256,
                      KeyProperties.DIGEST_SHA512)
              .build());
    }

    KeyPair kp = kpg.generateKeyPair();
    PublicKey publicKey = kp.getPublic();
    PrivateKey privateKey = kp.getPrivate();
    pub = publicKey;
    pri = privateKey;
    Log.d("tag", "Public key: " + publicKey.toString());
    Log.d("tag", "Private key: " + privateKey.toString());


    /*byte[] pKbytes = Base64.encode(publicKey.getEncoded(), Base64.DEFAULT);
    String pK = new String(pKbytes);
    String pubKeyFormated = "-----BEGIN PUBLIC KEY-----\n" + pK + "-----END PUBLIC KEY-----\n";
    Log.d("tag", "Public key FORMATTED: " + pubKeyFormated);*/

        /*byte[] privKeyBytes = Base64.encode(privateKey.getEncoded(), Base64.DEFAULT);
        String privK = new String(privKeyBytes);
        String privKeyFormated = "-----BEGIN PRIVATE KEY-----\n" + privK + "-----END PRIVATE KEY-----\n";
        Log.d(KEY_TAG, "Private key FORMATTED: " + privKeyFormated);*/

  }

  void genTag() {
    ByteBuffer tag;
    String name = edName.getText().toString();

    if (!hasKey || edUUID.getText().toString().length() == 0 || name.length() == 0 ||
        edEuros.getText().toString().length() == 0 || edCents.getText().toString().length() == 0) {
      tvNoKey.setText(R.string.msg_empty);
      return;
    }
    int len = 4 + 16 + 4 + 4 + 1 + edName.getText().toString().length();
    int l = edName.getText().toString().length();
    if (l > 35)
      name = edName.getText().toString().substring(0, 35);
    tag = ByteBuffer.allocate(len);
    tag.putInt(Constants.tagId);
    tag.putLong(productUUID.getMostSignificantBits());
    tag.putLong(productUUID.getLeastSignificantBits());
    tag.putInt(Integer.parseInt(edEuros.getText().toString()));
    tag.putInt(Integer.parseInt(edCents.getText().toString()));
    tag.put((byte)name.length());
    tag.put(name.getBytes(StandardCharsets.ISO_8859_1));

    try {
      Cipher cipher = Cipher.getInstance(Constants.ENC_ALGO);
      cipher.init(Cipher.ENCRYPT_MODE, pri);
      encTag = cipher.doFinal(tag.array());
      tvKey.setText(getResources().getString(R.string.msg_enctag, encTag.length, byteArrayToHex(encTag)));
      tvNoKey.setText("");
    }
    catch (Exception e) {
      tvNoKey.setText(e.toString());
    }
    Intent qrAct = new Intent(this, QRTag.class);
    qrAct.putExtra("data", encTag);
    startActivity(qrAct);
  }

  void decTag() {
    byte[] clearTag;

    if (encTag == null || encTag.length == 0) {
      tvNoKey.setText(R.string.msg_notag);
      return;
    }
    try {
      Cipher cipher = Cipher.getInstance(Constants.ENC_ALGO);
      cipher.init(Cipher.DECRYPT_MODE, pub);
      clearTag = cipher.doFinal(encTag);
    }
    catch (Exception e) {
      tvNoKey.setText(e.getMessage());
      return;
    }
    ByteBuffer tag = ByteBuffer.wrap(clearTag);
    int tId = tag.getInt();
    UUID id = new UUID(tag.getLong(), tag.getLong());
    int euros = tag.getInt();
    int cents = tag.getInt();
    byte[] bName = new byte[tag.get()];
    tag.get(bName);
    String name = new String(bName, StandardCharsets.ISO_8859_1);

    String text = "DecTag (" + clearTag.length + "):\n" + byteArrayToHex(clearTag) + "\n\n" +
                  ((tId==Constants.tagId)?"correct":"wrong") + "\n" +
                  "ID: " + id.toString() + "\n" +
                  "Name: " + name + "\n" +
                  "Price: €" + euros + "." + cents;
    tvNoKey.setText("");
    tvKey.setText(text);
  }

  void genKeys() {
    /*String text;

    try {
      Calendar start = new GregorianCalendar();
      Calendar end = new GregorianCalendar();
      end.add(Calendar.YEAR, 20);
      KeyPairGenerator kgen = KeyPairGenerator.getInstance(Constants.KEY_ALGO, Constants.ANDROID_KEYSTORE);
      AlgorithmParameterSpec spec = new KeyPairGeneratorSpec.Builder(this)
          .setKeySize(Constants.KEY_SIZE)
          .setAlias(Constants.keyname)
          .setSubject(new X500Principal("CN=" + Constants.keyname))
          .setSerialNumber(BigInteger.valueOf(Constants.CERT_SERIAL))
          .setStartDate(start.getTime())
          .setEndDate(end.getTime())
          .build();
      kgen.initialize(spec);
      KeyPair kp = kgen.generateKeyPair();
      pri = kp.getPrivate();                                         // private key in a Java class (PrivateKey)
      pub = kp.getPublic();                                          // the corresponding public key in a Java class (PublicKey)
      hasKey = true;
    }
    catch (Exception e) {
      tvNoKey.setText(e.getMessage());
      return;
    }
    findViewById(R.id.bt_send).setEnabled(hasKey);
    findViewById(R.id.bt_showkey).setEnabled(hasKey);
    findViewById(R.id.bt_genkey).setEnabled(!hasKey);
    tvNoKey.setText("");
    byte[] modulus = ((RSAPublicKey)pub).getModulus().toByteArray();
    text = "Modulus (" + modulus.length + "):\n";
    text += byteArrayToHex(modulus) + "\n";
    text += "Public Exponent:\n";
    text += byteArrayToHex(((RSAPublicKey)pub).getPublicExponent().toByteArray()) + "\n";
    tvKey.setText(text);*/
  }

  void sendKey() {
    X509Certificate cert;

    try {
        KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
      if (entry != null) {
        cert = (X509Certificate)((KeyStore.PrivateKeyEntry)entry).getCertificate();
        tvKey.setText(cert.toString());
        Intent intent = new Intent(this, NFCSendActivity.class);
        intent.putExtra("message", cert.getEncoded());
        intent.putExtra("mime", "application/nfc.feup.apm.pubkeyfortag");
        startActivity(intent);
      }
    }
    catch (Exception e) {
      tvNoKey.setText(e.getMessage());
    }
  }

  void showKey() {
    try {
      KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
      ks.load(null);
      KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
      if (entry != null) {
        X509Certificate cert = (X509Certificate)((KeyStore.PrivateKeyEntry)entry).getCertificate();
        byte[] encCert = cert.getEncoded();
        String strCert = cert.toString();
        String b64Cert = Base64.encodeToString(encCert, Base64.DEFAULT);
        String text = "cert(b64): " + b64Cert + "\n\n" + strCert;
        tvKey.setText(text);
        text = "-----BEGIN CERTIFICATE-----\n" + b64Cert +
               "-----END CERTIFICATE-----\n";
        Log.d(TAG, text);
      }
    }
    catch (Exception e) {
      tvNoKey.setText(e.getMessage());
    }
  }

  String byteArrayToHex(byte[] ba) {
    StringBuilder sb = new StringBuilder(ba.length * 2);
    for(byte b: ba)
      sb.append(String.format("%02x", b));
    return sb.toString();
  }
}
