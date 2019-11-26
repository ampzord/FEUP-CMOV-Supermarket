package fe.up.pt.supermarket.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.UUID;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.models.User;
import fe.up.pt.supermarket.models.Voucher;
import fe.up.pt.supermarket.utils.Constants;
import fe.up.pt.supermarket.utils.HttpsTrustManagerUtils;
import fe.up.pt.supermarket.utils.KeyStoreUtils;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;

public class LoginActivity extends AppCompatActivity {
    private Button btLogin;
    private Button btRegister;
    private EditText username;
    private EditText password;

    private static String TAG_LOGIN = "TAG_LOGIN";

    public static boolean hasServerKey = false;
    public static PublicKey SERVER_CERTIFICATE;
    public static User user;

    //public static String URL = "https://192.168.1.12:3001/api"; //HOME
    public static String URL = "https://10.227.154.87:3001/api"; //FEUP
    //public static String URL = "https://grisly-mummy-10353.herokuapp.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        user = new User();

        btLogin = findViewById(R.id.bt_send_login);
        btRegister = findViewById(R.id.bt_register);
        username = findViewById(R.id.login_username);
        password = findViewById(R.id.login_password);
        btLogin.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        if (MultipleClicksUtils.prevent()) return;
                        sendLoginRequest();
                    }
                });

        btRegister.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        if (MultipleClicksUtils.prevent())
                            return;
                        try {
                            KeyStoreUtils.getAllKeyStoreKeys();
                            //KeyStoreUtils.deleteKey("ServerKey");
                        } catch (CertificateException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (KeyStoreException e) {
                            e.printStackTrace();
                        }
                        sendToRegister();
                    }
                });

    }

    private void sendLoginRequest() {
        HttpsTrustManagerUtils.allowAllSSL();
        RequestQueue queue = Volley.newRequestQueue(this);
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username.getText().toString());
            jsonBody.put("password", password.getText().toString());

            String newURL = URL + "/login";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, newURL, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG_LOGIN, response.toString());
                            KeyStoreUtils.getServerKeyFromKeyStore();
                            //getUserKey()
                            if (user.privateKey == null && user.publicKey == null) {
                                getUserKeys(username.getText().toString());
                            }
                            //TODO PROFILE
                            readFromFileVouchers(getApplicationContext(), user.uuid + "_vouchers");

                            user.uuid = UUID.fromString(readFromFileUUID(getApplicationContext(), username.getText().toString()));
                            user.username = username.getText().toString();
                            sendToMainMenu();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(TAG_LOGIN, volleyError.toString());
                            if (volleyError.networkResponse != null) {
                                Log.d(TAG_LOGIN, "Status Code Error: " + volleyError.networkResponse.statusCode);
                                Log.d(TAG_LOGIN, "Server Error Response: " + new String(volleyError.networkResponse.data));
                                Toast.makeText(getApplicationContext(), new String(volleyError.networkResponse.data), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            Log.d(TAG_LOGIN, "Error caught: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void getUserKeys(String username) {
        try {
            boolean hasKey;
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(username, null);
            hasKey = (entry != null);
            if (hasKey) {
                user.publicKey = ((KeyStore.PrivateKeyEntry)entry).getCertificate().getPublicKey();
                user.privateKey = ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();
            }
        }
        catch (Exception e) {
            Log.d(TAG_LOGIN, "Error caught getting user key from keystore.");
        }
    }

    private String readFromFileUUID(Context context, String filename) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.d(TAG_LOGIN, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG_LOGIN, "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void sendToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }

    private void sendToRegister() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
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
