package com.example.supermarket.supermarket.terminal;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.supermarket.supermarket.terminal.model.Product;
import com.example.supermarket.supermarket.terminal.model.Transaction;
import com.example.supermarket.supermarket.terminal.utils.HttpsTrustManagerUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.Cipher;

public class MainActivity extends AppCompatActivity {
  static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
  boolean hasKey = false;
  PublicKey pub;

  //---------
  //private TextView tv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //tv = findViewById(R.id.tv_text);

    readKey();
  }

  /* The NFC messages are received in their own activities and sent to the MainActivity */
  @Override
  public void onResume() {
    super.onResume();
    int type = getIntent().getIntExtra("type", 0);
    if (type == 1)
      showAndStoreKey(getIntent().getByteArrayExtra("cert"));                // get the NFC message (public key certificate)
  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.mn_scan)
      scanQRCode();
    return true;
  }

  void readKey() {
    TextView tvTitle = findViewById(R.id.tv_title);

    try {
      KeyStore keyStore = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
      keyStore.load(null);
      Certificate cert = keyStore.getCertificate(Constants.keyAlias);
      if (cert != null) {
        pub = cert.getPublicKey();
        hasKey = true;
        //tvTitle.setText("User public key arrived!");
      }
    } catch (Exception e) {
      tvTitle.setText(e.getMessage());
    }
    /*if (hasKey) {
      String text = "Public Key:\nModulus: " + byteArrayToHex(((RSAPublicKey)pub).getModulus().toByteArray()) + "\n" +
              "Exponent: " + byteArrayToHex(((RSAPublicKey)pub).getPublicExponent().toByteArray());
      ((TextView)findViewById(R.id.tv_text)).setText(text);
    }*/
  }

  void showAndStoreKey(byte[] cert) {
    TextView tvTitle = findViewById(R.id.tv_title);

    try {
      /*int i = 0;
      String user = "user" + i;
      i++;*/
      KeyStore keyStore = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
      keyStore.load(null);
      X509Certificate x509 = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(cert));
      keyStore.setEntry(Constants.keyAlias, new KeyStore.TrustedCertificateEntry(x509), null);
      pub = x509.getPublicKey();
      hasKey = true;
      //tvTitle.setText(R.string.msg_keyinfo);
    } catch (Exception e) {
      tvTitle.setText(e.getMessage());
    }
    if (hasKey) {
      //Toast.makeText(getApplicationContext(), "A user public key has arrived!", Toast.LENGTH_SHORT).show();
    }
    /*String text = "Public Key:\nModulus: " + byteArrayToHex(((RSAPublicKey)pub).getModulus().toByteArray()) + "\n" +
            "Exponent: " + byteArrayToHex(((RSAPublicKey)pub).getPublicExponent().toByteArray());
    ((TextView)findViewById(R.id.tv_text)).setText(text);*/
  }

  public void scanQRCode() {
    try {
      Intent intent = new Intent(ACTION_SCAN);
      intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
      startActivityForResult(intent, 0);
    } catch (ActivityNotFoundException anfe) {
      showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
    }
  }

  private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
    AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
    downloadDialog.setTitle(title);
    downloadDialog.setMessage(message);
    downloadDialog.setPositiveButton(buttonYes, (d, i) -> {
      Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      act.startActivity(intent);
    });
    downloadDialog.setNegativeButton(buttonNo, null);
    return downloadDialog.show();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 0) {
      if (resultCode == RESULT_OK) {
        String contents = data.getStringExtra("SCAN_RESULT");
        if (contents != null)
          decodeAndShow(contents.getBytes(StandardCharsets.ISO_8859_1));
      }
    }
  }

  void decodeAndShow(byte[] encTag) {
    TextView tvTitle = findViewById(R.id.tv_title);
    byte[] clearTag;

    try {
      Cipher cipher = Cipher.getInstance(Constants.ENC_ALGO);
      cipher.init(Cipher.DECRYPT_MODE, pub);
      clearTag = cipher.doFinal(encTag);
    } catch (Exception e) {
      Log.d("INFO", "Error decrypting QR code of checkout.");
      tvTitle.setText(e.getMessage());
      return;
    }

    ByteBuffer tag = ByteBuffer.wrap(clearTag);
    //int tId = tag.getInt();
    long most0 = tag.getLong();
    long less0 = tag.getLong();
    UUID transactionID = new UUID(most0, less0);
    long most = tag.getLong();
    long less = tag.getLong();
    UUID user_id = new UUID(most, less);
    Log.d("INFO", "uuid: " + user_id.toString());
    float cost = tag.getFloat();
    //long most2 = tag.getLong();
    //long less2 = tag.getLong();
    //UUID voucher_id = new UUID(most2, less2);
    int discount = tag.getInt();
    int sizeList = tag.getInt();
    Transaction transaction = new Transaction();
    for (int i = 0; i < sizeList; i++) {
      //UUID
      long most_pro = tag.getLong();
      long less_pro = tag.getLong();
      UUID product_uuid = new UUID(most_pro, less_pro);
      //cost of product
      Float cost_product = tag.getFloat();
      //name of product
      byte l = tag.get();
      byte[] bName = new byte[l];
      tag.get(bName);
      String name = new String(bName, StandardCharsets.ISO_8859_1);
      Product pro = new Product(name, cost_product, product_uuid.toString());
      transaction.products.add(pro);
      Log.d("INFO", "name: " + name);
    }


    transaction.id = transactionID;
    transaction.user.uuid = user_id;
    transaction.totalCost = cost;
    transaction.discount = discount;

    sendServerInformation(transaction);

    Log.d("INFO", "sizeList: " + sizeList);


    /*String text = "Read Tag (" + clearTag.length + "):\n" + byteArrayToHex(clearTag) + "\n\n" +
            ((tId == Constants.tagId) ? "correct" : "wrong") + "\n" +
            "ID: " + id.toString() + "\n" +
            "Name: " + name + "\n" +
            "Price: €" + euros + "." + cents;
    ((TextView) findViewById(R.id.tv_text)).setText(text);

    Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();*/
  }

  public void sendServerInformation(Transaction transaction) {
    HttpsTrustManagerUtils.allowAllSSL();
    RequestQueue queue = Volley.newRequestQueue(this);
    try {
      JSONObject jsonBody = new JSONObject();
      jsonBody.put("id", transaction.id.toString());
      jsonBody.put("price", Float.toString(transaction.totalCost));
      jsonBody.put("user_uuid", transaction.user.uuid.toString());
      jsonBody.put("discount", Integer.toString(transaction.discount));

      JSONArray array = new JSONArray();

      for(int i=0;i<transaction.products.size();i++){
        JSONObject obj = new JSONObject();
        try {
          obj.put("productName",transaction.products.get(i).name);
          obj.put("productUUID",transaction.products.get(i).s_uuid);
          obj.put("productCost",transaction.products.get(i).cost);
        } catch (JSONException e) {
          e.printStackTrace();
        }
        array.put(obj);
      }

      String newURL = Constants.URL + "/transaction";
      jsonBody.put("Products",array);

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, newURL, jsonBody,
              new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                  Log.d("TRANSACTION", response.toString());
                  /*KeyStoreUtils.getServerKeyFromKeyStore();
                  getUserUUID(username.getText().toString());
                  RegistrationActivity.user.username = username.getText().toString();
                  sendToMainMenu();*/
                }
              },
              new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                  Log.d("TRANSACTION", volleyError.toString());
                  if (volleyError.networkResponse != null) {
                    Log.d("TRANSACTION", "Status Code Error: " + volleyError.networkResponse.statusCode);
                    Log.d("TRANSACTION", "Server Error Response: " + new String(volleyError.networkResponse.data));
                    Toast.makeText(getApplicationContext(), new String(volleyError.networkResponse.data), Toast.LENGTH_SHORT).show();
                  }
                }
              });

      queue.add(jsonObjectRequest);

    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  String byteArrayToHex(byte[] ba) {                              // converter
    StringBuilder sb = new StringBuilder(ba.length * 2);
    for (byte b : ba)
      sb.append(String.format("%02x", b));
    return sb.toString();
  }


}




//--------------------------------------------------------------

  /* The NFC messages are received in their own activities and sent to the MainActivity */
  /*@Override
  public void onResume() {
    super.onResume();
    int type = getIntent().getIntExtra("type", 0);        // type of NFC message (key(1) or order(2))
    switch (type) {
      case 1:
        showKey(getIntent().getByteArrayExtra("key"));                // get the NFC message (key modulus)
        break;
      case 2:
        showOrder(getIntent().getByteArrayExtra("order"));            // get the NFC message (signed order)
        break;
      default:
        tv.setText(R.string.tv_waiting);
    }
  }

  @Override
  public void onNewIntent(Intent intent) {       // if the intent is delivered to the current instance
    setIntent(intent);
  }

  void showKey(byte[] modulus) {
    String error = "";
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");        // to build a key object we need a KeyFactory object
      // the key raw values (as BigIntegers) are used to build an appropriate KeySpec
      RSAPublicKeySpec RSAPub = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger("65537"));
      pub = keyFactory.generatePublic(RSAPub);                   // the KeyFactory is used to build the key object from the key spec
    }
    catch (Exception ex) {
      error = ex.getMessage();
    }
    String text = "Public Key:\nModulus (";
    text += modulus.length;
    text += "):\n";
    text += byteArrayToHex(modulus);
    text += "\nExponent: 010001\n\n";
    text += error;
    //tv.setText(text);                                               // show the raw values of key components (in hex)
  }

  void showOrder(byte[] order) {
    /*String error = "";
    boolean validated = false;
    StringBuilder sb = new StringBuilder();
    int nr = order[0];                                              // get the nr of different products (first position)

    for (int k=1; k<=nr; k++) {
      sb.append(" - ");
      sb.append(products[order[k]-1]);                              // get the name of each product from the type
      sb.append("\n");
    }
    if (pubKey == null)
      sb.append("\nMissing pub key+.");
    else {
      byte[] mess = new byte[nr+1];                                // extract the order and the signature from the all message
      byte[] sign = new byte[keysize/8];
      ByteBuffer bb = ByteBuffer.wrap(order);
      bb.get(mess, 0, nr+1);
      bb.get(sign, 0, keysize/8);
      try {
        Signature sg = Signature.getInstance("SHA256WithRSA");      // verify the signature with the public key
        sg.initVerify(pubKey);
        sg.update(mess);
        validated = sg.verify(sign);
      }
      catch (Exception ex) {
        error = "\n" + ex.getMessage();
      }
    }
    sb.append("\nValidated = ");
    sb.append(validated);
    sb.append(error);
    tv.setText(sb.toString());             */                       // show order and validation*/
/*
    String error = "";
    boolean validated = false;
    StringBuilder sb = new StringBuilder();
    int nr = order[0];                                             // get the nr of different products (first position)
    Log.d("CHECKOUT", "NR: " + nr);

    for (int k=1; k<=nr; k++) {
      sb.append(" - ");
      //toProduct();                              // get the name of each product from the type
      sb.append("\n");
    }
  }

  public static Product toProduct(byte[] stream) {
    Product stu = null;

    try (ByteArrayInputStream bais = new ByteArrayInputStream(stream);
         ObjectInputStream ois = new ObjectInputStream(bais);) {
      stu = (Product) ois.readObject();
    } catch (IOException e) {
      // Error in de-serialization
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // You are converting an invalid stream to Student
      e.printStackTrace();
    }
    return stu;
  }

  String byteArrayToHex(byte[] ba) {                              // converter
    StringBuilder sb = new StringBuilder(ba.length * 2);
    for (byte b : ba)
      sb.append(String.format("%02x", b));
    return sb.toString();
  }
}
*/