package fe.up.pt.supermarket.utils;

class Constants {
  static final String ANDROID_KEYSTORE = "AndroidKeyStore";
  static final int KEY_SIZE = 512;
  static final String KEY_ALGO = "RSA";
  static final int CERT_SERIAL = 12121212;
  static final String ENC_ALGO = "RSA/NONE/PKCS1Padding";
  //static final String ENC_ALGO = "AES/CBC/PKCS1Padding";
  static String keyAlias = "ServerKey";
  static int tagId = 0x41636D65;        // equal to "Acme"
}
