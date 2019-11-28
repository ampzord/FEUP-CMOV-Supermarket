package fe.up.pt.supermarket.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.activities.LoginActivity;
import fe.up.pt.supermarket.activities.MainMenuActivity;
import fe.up.pt.supermarket.models.Voucher;

import static fe.up.pt.supermarket.activities.LoginActivity.user;

public class QRTag extends AppCompatActivity {
  private final String TAG = "QR_Code";

  ImageView qrCodeImageview;
  String qr_content = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_qrtag);
    qrCodeImageview = findViewById(R.id.img_qr_code);

    byte[] content = getIntent().getByteArrayExtra("data");
    qr_content = new String(content, StandardCharsets.ISO_8859_1);

    Thread t = new Thread(() -> {              // do the creation in a new thread to avoid ANR Exception
      final Bitmap bitmap;
      try {
        bitmap = encodeAsBitmap(qr_content);
        runOnUiThread(() -> {                  // runOnUiThread method used to do UI task in main thread.
          qrCodeImageview.setImageBitmap(bitmap);
        });
      }
      catch (Exception e) {
        Log.d(TAG, e.getMessage());
      }
    });
    if (LoginActivity.user.selectedVoucher != null) {
      ArrayList<String> write = new ArrayList<>();
      for (int i = 0; i < user.vouchers.size(); i++) {
        String value = "";
        String uuid = user.vouchers.get(i).uuid.toString();
        String disc = Integer.toString(user.vouchers.get(i).discount_percentage);
        String used_string = "false";
        if (user.vouchers.get(i).used)
          used_string = "true";

        if (user.vouchers.get(i).uuid == user.selectedVoucher.uuid) {
          Log.d("TAG_VOUCHER", "Updating voucher by selectedVoucher");
          used_string = "true";
        }

        value = uuid + "," + disc + "," + used_string;
        Log.d("TAG_VOUCHER", "QRTAG - Voucher line Writing: " + value);
        write.add(value);
      }
      writeToFileVouchersAuxiliary(getApplicationContext(), user.uuid + "_vouchers", write);
      readFromFileVouchers(getApplicationContext(), user.uuid + "_vouchers");

      MainMenuActivity.vouchersList = new ArrayList<>();
      MainMenuActivity.vouchersList.add("No Voucher");
      for (int i = 0; i < user.vouchers.size(); i++) {
        if (!user.vouchers.get(i).used)
          MainMenuActivity.vouchersList.add(user.vouchers.get(i).toString());
      }
      MainMenuActivity.spinnerArrayAdapter = new ArrayAdapter<>(
              this,R.layout.voucher_spinner_item,MainMenuActivity.vouchersList);
      MainMenuActivity.spinnerArrayAdapter.setDropDownViewResource(R.layout.voucher_spinner_item);
      MainMenuActivity.voucherSpinner.setAdapter(MainMenuActivity.spinnerArrayAdapter);
      MainMenuActivity.spinnerArrayAdapter.notifyDataSetChanged();
    }

    user.shoppingCart = new ArrayList<>();
    MainMenuActivity.adapter.clear();
    MainMenuActivity.adapter.notifyDataSetChanged();
    t.start();
  }

  Bitmap encodeAsBitmap(String str) throws WriterException {
    int DIMENSION = 1000;
    BitMatrix result;

    Hashtable<EncodeHintType, String> hints = new Hashtable<>();
    hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
    try {
      result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, DIMENSION, DIMENSION, hints);
    }
    catch (IllegalArgumentException iae) {
      return null;
    }
    int w = result.getWidth();
    int h = result.getHeight();
    int[] pixels = new int[w * h];
    for (int y = 0; y < h; y++) {
      int offset = y * w;
      for (int x = 0; x < w; x++) {
        pixels[offset + x] = result.get(x, y) ? getResources().getColor(R.color.colorPrimary) : Color.WHITE;
      }
    }
    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    return bitmap;
  }

  private void writeToFileVouchersAuxiliary(Context context, String filename, ArrayList<String> data) {
    try {
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
      for (int i = 0; i < data.size(); i++) {
        outputStreamWriter.write(data.get(i));
        outputStreamWriter.write("\n");
      }
      outputStreamWriter.close();
    }
    catch (IOException e) {
      Log.d("TAG_VOUCHER", "File write failed: " + e.toString());
    }
  }

  private String readFromFileVouchers(Context context, String filename) {

    String ret = "";

    try {
      InputStream inputStream = context.openFileInput(filename);

      if ( inputStream != null ) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String receiveString = "";
        StringBuilder stringBuilder = new StringBuilder();
        user.vouchers = new ArrayList<>();
        while ( (receiveString = bufferedReader.readLine()) != null ) {
          stringBuilder.append(receiveString);
          //1 linha
          Log.d("TAG_VOUCHER", "QRTAG - Reading from file: " + receiveString);
          String[] vouchersArray = receiveString.split(",");
          String uuid_voucher = vouchersArray [0];
          String discount_voucher = vouchersArray [1];
          String used_voucher = vouchersArray [2];
          Voucher temp_voucher = new Voucher();
          temp_voucher.discount_percentage = Integer.parseInt(discount_voucher);
          temp_voucher.uuid = UUID.fromString(uuid_voucher);

          Log.d("TAG_VOUCHER", "QRTAG - String used value: " + used_voucher);

          if (used_voucher.equals("true"))
            temp_voucher.used = true;
          else
            temp_voucher.used = false;

          Log.d("TAG_VOUCHER", "QRTAG - Value of Used in Voucher: " + temp_voucher.used);

          //if (temp_voucher.used == false) {
          user.vouchers.add(temp_voucher);
            //Log.d("TAG_VOUCHER", "QRTAG - Adding voucher to list: " + temp_voucher.toString());
          //}
        }

        inputStream.close();
        ret = stringBuilder.toString();
      }
    }
    catch (FileNotFoundException e) {
      Log.d("TAG_VOUCHER", "File not found: " + e.toString());
    } catch (IOException e) {
      Log.e("TAG_VOUCHER", "Can not read file: " + e.toString());
    }

    return ret;
  }
}
