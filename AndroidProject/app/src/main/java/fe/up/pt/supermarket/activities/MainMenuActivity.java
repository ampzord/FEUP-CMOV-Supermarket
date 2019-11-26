package fe.up.pt.supermarket.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;

import fe.up.pt.supermarket.adapter.ProductAdapter;
import fe.up.pt.supermarket.models.Product;
import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.models.Voucher;
import fe.up.pt.supermarket.utils.Constants;
import fe.up.pt.supermarket.utils.HttpsTrustManagerUtils;
import fe.up.pt.supermarket.utils.KeyStoreUtils;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;
import fe.up.pt.supermarket.utils.NFCSendActivity;
import fe.up.pt.supermarket.utils.QRTag;

import static fe.up.pt.supermarket.activities.LoginActivity.URL;
import static fe.up.pt.supermarket.activities.LoginActivity.user;

public class MainMenuActivity extends AppCompatActivity {
    private ImageButton scanItem;
    private ImageButton profile;
    private ImageButton checkout_button;
    private ImageButton coupons_button;
    private ImageButton logout;
    private Switch discountSwitch;
    public Button clearList;
    public ArrayAdapter<String> spinnerArrayAdapter;
    public TextView totalCost;

    private Spinner voucherSpinner;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    public static ProductAdapter adapter;
    RecyclerView recyclerView;

    public List<String> vouchersList;
    public static DecimalFormat df2 = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new ProductAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setProductsInfo(user.shoppingCart);

        discountSwitch = this.findViewById(R.id.coupon_switch);
        voucherSpinner = this.findViewById(R.id.coupon_spinner);
        profile = findViewById(R.id.profile_button);
        //sendKey = findViewById(R.id.sendKey);
        scanItem = findViewById(R.id.bt_scan_item);
        checkout_button = findViewById(R.id.checkout_button);
        clearList = findViewById(R.id.clearList);
        coupons_button = findViewById(R.id.coupons_button);
        totalCost = findViewById(R.id.totalCost);

        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    user.discount = true;
                } else {
                    user.discount = false;
                }
            }
        });

        if (user.shoppingCart.size() == 0) {
            clearList.setVisibility(View.GONE);
            totalCost.setVisibility(View.GONE);
        }

        vouchersList = new ArrayList<>();
        vouchersList.add("No Voucher");
        for (int i = 0; i < user.vouchers.size(); i++) {
            if (user.vouchers.get(i).used == true)
                break;
            vouchersList.add(user.vouchers.get(i).toString());
        }
        spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.voucher_spinner_item,vouchersList);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.voucher_spinner_item);
        voucherSpinner.setAdapter(spinnerArrayAdapter);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent())
                    return;
                sendToProfilePage();
            }
        });

        coupons_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent())
                    return;
                updateVouchers();
            }
        });

        scanItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent())
                    return;
                if (user.shoppingCart.size() == 10) {
                    Toast.makeText(MainMenuActivity.this, "Max is 10 items.", Toast.LENGTH_SHORT).show();
                    return;
                }
                scan(true);
                if (user.shoppingCart.size() != 0) {
                    totalCost.setText("Total Cost: " + df2.format(user.getTotalCost()) + "€");
                    clearList.setVisibility(View.VISIBLE);
                    totalCost.setVisibility(View.VISIBLE);
                }
            }
        });
        checkout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent())
                    return;
                if (user.shoppingCart.size() == 0) {
                    Toast.makeText(MainMenuActivity.this, "Need at least 1 item", Toast.LENGTH_SHORT).show();

                    return;
                }

                sendUserPublicKeyToTerminal();
            }
        });
        clearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.shoppingCart = new ArrayList<>();
                adapter.clear();
                adapter.notifyDataSetChanged();
                clearList.setVisibility(View.GONE);
                totalCost.setVisibility(View.GONE);
            }
        });

        voucherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch(parentView.getId()) {
                    case R.id.coupon_spinner:
                        if (position != 0) {
                            user.selectedVoucher = user.vouchers.get(position - 1);
                            user.selectedVoucherHelper = true;
                        }
                        if (position == 0) {
                            user.selectedVoucher = null;
                            user.selectedVoucherHelper = false;
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //Toast.makeText(MainMenuActivity.this, "nada", Toast.LENGTH_LONG).show();
            }

        });


    }

    void sendUserPublicKeyToTerminal() {
        X509Certificate cert;

        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(user.username, null);
            if (entry != null) {
                cert = (X509Certificate) ((KeyStore.PrivateKeyEntry) entry).getCertificate();
                //tvKey.setText(cert.toString());
                Intent intent = new Intent(MainMenuActivity.this, NFCSendActivity.class);
                intent.putExtra("message", cert.getEncoded());
                intent.putExtra("mime", "application/nfc.fe.up.pt.pubkeyforterminal");
                startActivity(intent);
            }
        }
        catch (Exception e) {
            //tvNoKey.setText(e.getMessage());
            Log.d("MAIN_MENU", "Error loading user public key to send to Terminal.");
        }
    }

    /*

        @Override
    public void onBackPressed() {
        if(homepageBinding.fab.isExpanded())
            homepageBinding.fab.setExpanded(false);
        else if(filterBottomSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED)
            filterBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        else
            super.onBackPressed();
     */

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        //bundle.putCharSequence("Message", message.getText());
        //bundle.putParcelableArrayList("shoppingCart", RegistrationActivity.user.shoppingCart);
        //adapter.setProductsInfo(RegistrationActivity.user.shoppingCart);
    }

    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        //ArrayList<Product> getShoppingCart = bundle.getSerializable("ShoppingCart");
        //RegistrationActivity.user.shoppingCart = bundle.getParcelableArrayList("shoppingCart");
        //adapter.setProductsInfo(RegistrationActivity.user.shoppingCart);
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.setProductsInfo(user.shoppingCart);
        adapter.notifyDataSetChanged();
        if (user.shoppingCart.size() == 0) {
            clearList.setVisibility(View.GONE);
            totalCost.setVisibility(View.GONE);
        }
        else {
            totalCost.setText("Total Cost: " + df2.format(user.getTotalCost()) + "€");
            clearList.setVisibility(View.VISIBLE);
            totalCost.setVisibility(View.VISIBLE);
        }
        spinnerArrayAdapter.notifyDataSetChanged();

        vouchersList = new ArrayList<>();
        vouchersList.add("No Voucher");
        for (int i = 0; i < user.vouchers.size(); i++) {
            if (user.vouchers.get(i).used == true)
                break;
            vouchersList.add(user.vouchers.get(i).toString());
        }
        spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.voucher_spinner_item,vouchersList);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.voucher_spinner_item);
        voucherSpinner.setAdapter(spinnerArrayAdapter);

    }

    public void scan(boolean qrcode) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }
        catch (ActivityNotFoundException anfe) {
            showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private static AlertDialog showDialog(final AppCompatActivity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, (dialogInterface, i) -> {
            Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            act.startActivity(intent);
        });
        downloadDialog.setNegativeButton(buttonNo, null);
        return downloadDialog.show();
    }

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

    private void decodeAndShow(byte[] encTag) {
        byte[] clearTag;

        try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, LoginActivity.SERVER_CERTIFICATE);
            clearTag = cipher.doFinal(encTag);
        }
        catch (Exception e) {
            Log.d("QRCODE", "Error doing cipher in qrcode.");
            return;
        }
        ByteBuffer tag = ByteBuffer.wrap(clearTag);
        int tId = tag.getInt();
        long most = tag.getLong();
        long less = tag.getLong();
        UUID id = new UUID(most, less);
        int euros = tag.getInt();
        int cents = tag.getInt();
        byte l = tag.get();
        byte[] bName = new byte[l];
        tag.get(bName);
        String name = new String(bName, StandardCharsets.ISO_8859_1);

        int tagId = 0x41636D65;
        String text = "Read Tag (" + clearTag.length + "):\n" + byteArrayToHex(clearTag) + "\n\n" +
                ((tId==tagId)?"correct":"wrong") + "\n" +
                "ID: " + id.toString() + "\n" +
                "Name: " + name + "\n" +
                "Price: €" + euros + "." + cents;
        //String name, Integer euros, Integer cents, UUID uuid
        //UUID uuidProduct = UUID.fromString(id.toString());
        Product pro = new Product(name, euros, cents, id.toString());
        Log.d("QRCODE", "Name: " + pro.name);
        Log.d("QRCODE", "euros: " + pro.euros);
        Log.d("QRCODE", "cents: " + pro.cents);
        Log.d("QRCODE", "UUID: " + pro.s_uuid);

        user.shoppingCart.add(pro);
        adapter.notifyDataSetChanged();
        //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
        //message.setText(name);
    }

    String byteArrayToHex(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length * 2);
        for(byte b: ba)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void sendToProfilePage() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(intent);
    }

    private void updateVouchers() {
        HttpsTrustManagerUtils.allowAllSSL();
        RequestQueue queue = Volley.newRequestQueue(this);

        String newURL = URL + "/vouchers/" + user.uuid;

        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                newURL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //save vouchers, on LOGIN load vouchers.

                        try {
                            JSONArray arr = response.getJSONArray("vouchers");
                            ArrayList<String> vouchers_array = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject jsonobject = arr.getJSONObject(i);
                                String uuid_voucher = jsonobject.getString("uuid");
                                String discount_number = jsonobject.getString("discount_number");
                                String used_voucher = jsonobject.getString("used");
                                String voucher = uuid_voucher + "," + discount_number + "," + used_voucher;
                                vouchers_array.add(voucher);
                            }

                            writeToFileVouchers(getApplicationContext(), user.uuid + "_vouchers", vouchers_array);
                            readFromFileVouchers(getApplicationContext(),user.uuid + "_vouchers");
                            vouchersList = new ArrayList<>();
                            vouchersList.add("No Voucher");
                            for (int i = 0; i < user.vouchers.size(); i++) {
                                if (user.vouchers.get(i).used == true)
                                    break;
                                vouchersList.add(user.vouchers.get(i).toString());
                            }
                            spinnerArrayAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),R.layout.voucher_spinner_item,vouchersList);
                            spinnerArrayAdapter.setDropDownViewResource(R.layout.voucher_spinner_item);
                            voucherSpinner.setAdapter(spinnerArrayAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("REST Error Response", error.toString());
                    }
                }


        );

        queue.add(objectRequest);
    }


    private void writeToFileVouchers(Context context, String filename, ArrayList<String> data) {
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
                    Log.d("TAG_VOUCHER", "Linha: " + receiveString);
                    String[] vouchersArray = receiveString.split(",");
                    String uuid_voucher = vouchersArray [0];
                    String discount_voucher = vouchersArray [1];
                    String used_voucher = vouchersArray [2];
                    Voucher temp_voucher = new Voucher();
                    temp_voucher.discount_percentage = Integer.parseInt(discount_voucher);
                    temp_voucher.uuid = UUID.fromString(uuid_voucher);
                    if (used_voucher == "true")
                        temp_voucher.used = true;
                    else
                        temp_voucher.used = false;

                    if (temp_voucher.used == false)
                        user.vouchers.add(temp_voucher);

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
