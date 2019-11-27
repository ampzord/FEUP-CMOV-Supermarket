package fe.up.pt.supermarket.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.adapter.ProductAdapter;
import fe.up.pt.supermarket.adapter.TransactionsAdapter;
import fe.up.pt.supermarket.models.Transaction;
import fe.up.pt.supermarket.models.Voucher;
import fe.up.pt.supermarket.utils.HttpsTrustManagerUtils;
import fe.up.pt.supermarket.utils.KeyStoreUtils;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;

import static fe.up.pt.supermarket.activities.LoginActivity.URL;
import static fe.up.pt.supermarket.activities.LoginActivity.hasServerKey;
import static fe.up.pt.supermarket.activities.LoginActivity.user;

public class ProfileActivity extends AppCompatActivity {

    public static TransactionsAdapter transaction_adapter;
    RecyclerView recyclerView;
    private Button coupons_button;
    private Button transactions_button;
    private TextView fullname;
    private TextView username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recyclerView = findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        transaction_adapter = new TransactionsAdapter(this);
        recyclerView.setAdapter(transaction_adapter);
        transaction_adapter.setTransactionInfo(user.transactions);

        coupons_button = findViewById(R.id.updateVouchersProfile);
        transactions_button = findViewById(R.id.updateTransactions);
        fullname = findViewById(R.id.given_name);
        this.username = findViewById(R.id.given_name2);

        fullname.setText(user.fullname);
        this.username.setText(user.username);

        coupons_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent())
                    return;
                updateVouchers();
            }
        });
        transactions_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent())
                    return;
                updateTransactions();
            }
        });
    }

    private void updateTransactions() {
        HttpsTrustManagerUtils.allowAllSSL();
        RequestQueue queue = Volley.newRequestQueue(this);

        String newURL = URL + "/transactions/" + user.uuid;

        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                newURL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //save vouchers, on LOGIN load vouchers.

                        try {
                            JSONArray arr = response.getJSONArray("transactions");
                            ArrayList<String> transac_array = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject jsonobject = arr.getJSONObject(i);
                                String transaction_uuid = jsonobject.getString("uuid");
                                String transaction_user_uuid = jsonobject.getString("user_uuid");
                                String transaction_price = jsonobject.getString("price");
                                String transaction_discount_used = jsonobject.getString("discount_used");
                                String transaction_size = jsonobject.getString("products_size");

                                String voucher_uuid = "";
                                if (jsonobject.has("voucher_uuid")) {
                                    voucher_uuid = jsonobject.getString("voucher_uuid");
                                }
                                String trasac = transaction_uuid + "," + transaction_user_uuid + "," + transaction_price
                                        + "," + transaction_discount_used + "," + transaction_size + "," + voucher_uuid;
                                transac_array.add(trasac);
                            }

                            writeToFileTransactions(getApplicationContext(), user.uuid + "_transactions", transac_array);
                            readFromFileTransactions(getApplicationContext(),user.uuid + "_transactions");

                            transaction_adapter.setTransactionInfo(user.transactions);
                            transaction_adapter.notifyDataSetChanged();

                            ProfileActivity.super.onResume();
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

                            MainMenuActivity.vouchersList = new ArrayList<>();
                            MainMenuActivity.vouchersList.add("No Voucher");
                            for (int i = 0; i < user.vouchers.size(); i++) {
                                if (!user.vouchers.get(i).used)
                                    MainMenuActivity.vouchersList.add(user.vouchers.get(i).toString());
                            }
                            MainMenuActivity.spinnerArrayAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),R.layout.voucher_spinner_item,MainMenuActivity.vouchersList);
                            MainMenuActivity.spinnerArrayAdapter.setDropDownViewResource(R.layout.voucher_spinner_item);
                            MainMenuActivity.voucherSpinner.setAdapter(MainMenuActivity.spinnerArrayAdapter);
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
                    if (used_voucher.equals("true"))
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

    private void writeToFileTransactions(Context context, String filename, ArrayList<String> data) {
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

    private String readFromFileTransactions(Context context, String filename) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                user.transactions = new ArrayList<>();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    //1 linha
                    Log.d("TAG_TRANSACTION", "Linha: " + receiveString);
                    String[] vouchersArray = receiveString.split(",");
                    String transac_uuid;
                    String trascan_user_uuid;
                    String transac_price;
                    String transac_discount_used;
                    String transac_products_size;
                    String transcan_voucher_uuid;

                    if (vouchersArray.length == 6) {
                        transac_uuid = vouchersArray [0];
                        trascan_user_uuid = vouchersArray [1];
                        transac_price = vouchersArray [2];
                        transac_discount_used = vouchersArray [3];
                        transac_products_size = vouchersArray [4];
                        transcan_voucher_uuid = vouchersArray [5];

                        Transaction temp_transaction = new Transaction();
                        temp_transaction.uuid = UUID.fromString(transac_uuid);
                        temp_transaction.totalCost = Double.parseDouble(transac_price);
                        Boolean disc = false;
                        if (transac_discount_used == "true")
                            disc = true;
                        temp_transaction.usedDiscount = disc;
                        temp_transaction.transactionSize = Integer.parseInt(transac_products_size);
                        temp_transaction.voucher_transac_uuid = transcan_voucher_uuid;

                        String discount_temp = user.getVoucherDiscountFromStringUUID(temp_transaction.voucher_transac_uuid);

                        temp_transaction.voucherUsedDiscount = discount_temp;

                        user.transactions.add(temp_transaction);
                    } else {
                        transac_uuid = vouchersArray [0];
                        trascan_user_uuid = vouchersArray [1];
                        transac_price = vouchersArray [2];
                        transac_discount_used = vouchersArray [3];
                        transac_products_size = vouchersArray [4];

                        Transaction temp_transaction = new Transaction();
                        temp_transaction.uuid = UUID.fromString(transac_uuid);
                        temp_transaction.totalCost = Double.parseDouble(transac_price);
                        Boolean disc = false;
                        if (Integer.parseInt(transac_discount_used) == 1)
                            disc = true;
                        temp_transaction.usedDiscount = disc;
                        temp_transaction.transactionSize = Integer.parseInt(transac_products_size);

                        user.transactions.add(temp_transaction);
                    }


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
