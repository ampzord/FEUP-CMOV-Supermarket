package fe.up.pt.supermarket.utils;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.Cipher;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.activities.LoginActivity;
import fe.up.pt.supermarket.activities.MainMenuActivity;

import static fe.up.pt.supermarket.activities.LoginActivity.user;

public class NFCSendActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    NfcAdapter nfcAdapter;
    String mimeType;
    byte[] message;

    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_processing_v2);

    // Check for available NFC Adapter
    nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    if (nfcAdapter == null) {
      Toast.makeText(getApplicationContext(), "NFC is not available on this device.", Toast.LENGTH_LONG).show();
      finish();
    }
    Bundle extras = getIntent().getExtras();
    mimeType = extras.getString("mime");
    message = extras.getByteArray("message");
    NdefMessage msg = new NdefMessage(new NdefRecord[] { createMimeRecord(mimeType, message) });

    nfcAdapter.setNdefPushMessage(msg, this);       // Register a NDEF message to be sent in P2P
    nfcAdapter.setOnNdefPushCompleteCallback((ev)->{       // when the message is sent ...
      runOnUiThread(() -> {
        Toast.makeText(getApplicationContext(), "Message sent.", Toast.LENGTH_LONG).show();
          //Toast.makeText(this, "helper: " + user.selectedVoucherHelper, Toast.LENGTH_LONG).show();
          generateCheckoutTag();
        /*user.shoppingCart = new ArrayList<>();
        MainMenuActivity.adapter.clear();*/
        finish();
      });
    }, this);
  }

  public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
    byte[] mimeBytes = mimeType.getBytes(StandardCharsets.ISO_8859_1);
    return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
  }

  void generateCheckoutTag() {
    byte[] encTag = new byte[0];
    ByteBuffer tag;
    int discount_int = 0;
    if (LoginActivity.user.discount)
      discount_int = 1;

    UUID qrCodeUUID = UUID.randomUUID();
    int n = LoginActivity.user.shoppingCart.size();
    int length;
    //if (user.selectedVoucherHelper) {
      //  length = 16 + 16 + 8 + 16 + 4 + 4 + 4;
    //}
    //else {
        length = 16 + 16 + 8 + 4 + 4 + 4;
    //}
    int voucher_int = 0;
    if (user.selectedVoucherHelper)
        voucher_int = 1;


    UUID newUUIDVoucher;
      if (user.selectedVoucherHelper) {
          newUUIDVoucher = user.selectedVoucher.uuid;
                  /*tag.putLong(LoginActivity.user.selectedVoucher.uuid.getMostSignificantBits()); // 8
          tag.putLong(LoginActivity.user.selectedVoucher.uuid.getLeastSignificantBits()); // 8 - Voucher UUID*/
      } else {
          newUUIDVoucher = qrCodeUUID;
          /*tag.putLong(LoginActivity.user.uuid.getMostSignificantBits()); // 8
          tag.putLong(LoginActivity.user.uuid.getLeastSignificantBits()); // 8 - User UUID*/
      }


    try {
      tag = ByteBuffer.allocate(length);
      /*tag.putLong(qrCodeUUID.getMostSignificantBits()); // 8
      tag.putLong(qrCodeUUID.getLeastSignificantBits()); // 8 - TRANSACTION UUID*/
      tag.putLong(LoginActivity.user.uuid.getMostSignificantBits()); // 8
      tag.putLong(LoginActivity.user.uuid.getLeastSignificantBits()); // 8 - User UUID
        tag.putLong(newUUIDVoucher.getMostSignificantBits()); // 8
        tag.putLong(newUUIDVoucher.getLeastSignificantBits()); // 8 - Voucher UUID
      tag.putDouble(LoginActivity.user.getTotalCost()); //8 - cost
        /*if (user.selectedVoucherHelper) {
            tag.putLong(LoginActivity.user.selectedVoucher.uuid.getMostSignificantBits()); // 8
            tag.putLong(LoginActivity.user.selectedVoucher.uuid.getLeastSignificantBits()); // 8 - Voucher UUID
        }
        else {
            tag.putLong(LoginActivity.user.uuid.getMostSignificantBits()); // 8
            tag.putLong(LoginActivity.user.uuid.getLeastSignificantBits()); // 8 - User UUID
        }*/
      tag.putInt(discount_int); //4 discount
        tag.putInt(voucher_int); // 4 use
      tag.putInt(n); //4 size of shoppingList


      /*tag.rewind();

      // print the ByteBuffer
      Log.d("MAIN_MENU", "Original ByteBuffer:  "
              + Arrays.toString(tag.array()));*/


      //Log.d("MAIN_MENU", "UserPrivateKey: " + LoginActivity.user.privateKey.toString());
      Cipher cipher = Cipher.getInstance(Constants.ENC_ALGO);
      cipher.init(Cipher.ENCRYPT_MODE, LoginActivity.user.privateKey);
      encTag = cipher.doFinal(tag.array());
    }
    catch (IllegalArgumentException e) {
      Log.d("MAIN_MENU", "IllegalArgumentException catched");
    }

    catch (ReadOnlyBufferException e) {
      Log.d("MAIN_MENU", "ReadOnlyBufferException catched");
    }
    catch (Exception e) {
      Toast.makeText(getApplicationContext(),"Error Generating Checkout QRCode.", Toast.LENGTH_SHORT).show();
      Log.d("MAIN_MENU", "Error Generating Checkout QRCode.");
    }

    Intent qrAct = new Intent(this, QRTag.class);
    qrAct.putExtra("data", encTag);
    startActivity(qrAct);
  }
}
