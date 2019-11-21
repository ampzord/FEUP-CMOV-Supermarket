package com.example.supermarket.supermarket.terminal;

class Constants {
  static final int KEY_SIZE = 512;
  static final String ANDROID_KEYSTORE = "AndroidKeyStore";
  static final String keyAlias = "UserKey";
  static final String ENC_ALGO = "RSA/NONE/PKCS1Padding";
  static int tagId = 0x41636D65;        // equal to "Acme"
  //static String URL = "https://10.227.159.111:3001/api"; //FEUP
  public static String URL = "https://192.168.1.12:3001/api"; //HOME

  //public static String URL = "https://localhost:3000/api";
  //public static String URL = "https://192.168.1.12:3001/api"; //HOME
  //public static String URL = "https://10.227.159.111:3001/api"; //FEUP
  //public static String URL = "https://grisly-mummy-10353.herokuapp.com";
}
