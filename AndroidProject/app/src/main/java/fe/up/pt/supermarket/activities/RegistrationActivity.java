package fe.up.pt.supermarket.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.security.auth.x500.X500Principal;

import fe.up.pt.supermarket.utils.Constants;
import fe.up.pt.supermarket.utils.HttpsTrustManagerUtils;
import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.utils.KeyStoreUtils;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;

import static fe.up.pt.supermarket.activities.LoginActivity.URL;
import static fe.up.pt.supermarket.activities.LoginActivity.hasServerKey;
import static fe.up.pt.supermarket.activities.LoginActivity.user;


public class RegistrationActivity extends AppCompatActivity {
    private final String TAG_REGISTER = "TAG_REGISTER";

    private Button register;
    private EditText firstName;
    private EditText lastName;
    private EditText username;
    private EditText password;
    private EditText password_conf;
    private EditText credit_card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);

        register = findViewById(R.id.bt_send_register);
        firstName = findViewById(R.id.edit_first_name);
        lastName = findViewById(R.id.edit_last_name);
        username = findViewById(R.id.edit_username);
        password = findViewById(R.id.edit_password);
        password_conf = findViewById(R.id.edit_password_conf);
        credit_card = findViewById(R.id.credit_card);

        register.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (MultipleClicksUtils.prevent()) return;
                        sendRegistrationRequest();
                    }
        });
    }

    private void sendRegistrationRequest() {
        HttpsTrustManagerUtils.allowAllSSL();
        RequestQueue queue = Volley.newRequestQueue(this);
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("fName", firstName.getText().toString());
            jsonBody.put("lName", lastName.getText().toString());
            jsonBody.put("username", username.getText().toString());
            jsonBody.put("password", password.getText().toString());
            jsonBody.put("password_conf", password_conf.getText().toString());
            jsonBody.put("credit_card", credit_card.getText().toString());

            String newURL = URL + "/register";

            if (!checkUserKeyExists(username.getText().toString())) {
                generateAndStoreKeys(username.getText().toString());
                user.certificate = KeyStoreUtils.getUserPublicKeyCertificate(username.getText().toString());
            }
            else {
                Log.d(TAG_REGISTER, "There already exists a KeyStore in this username.");
                user.certificate = "This user already exists in the database. A new Key was not created.";
            }

            jsonBody.put("public_key", user.certificate);

            //Log.d("RegistrationRequest", "URL: " + newURL);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, newURL, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String server_msg_response = response.getString("message");
                                Toast.makeText(getApplicationContext(), server_msg_response, Toast.LENGTH_SHORT).show();
                                JSONObject getUser = response.getJSONObject("user");
                                Object username = getUser.get("username");
                                Object uuid = getUser.get("uuid");
                                String s_username = username.toString();
                                String s_uuid = uuid.toString();
                                KeyStoreUtils.getServerKeyFromKeyStore();
                                if (!hasServerKey) {
                                    KeyStoreUtils.getCertificateFromServerMessage(response);
                                }
                                //saveUserUUID(getUser);
                                writeToFileUUID(getApplicationContext(),s_username,s_uuid);
                                user.uuid = UUID.fromString(s_uuid);
                                sendToLoginPage();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(TAG_REGISTER, volleyError.toString());
                            if (volleyError.networkResponse != null) {
                                Log.d(TAG_REGISTER, "Status Code Error: " + volleyError.networkResponse.statusCode);
                                Log.d(TAG_REGISTER, "Server Error Response: " + new String(volleyError.networkResponse.data));
                                Toast.makeText(getApplicationContext(), new String(volleyError.networkResponse.data), Toast.LENGTH_SHORT).show();
                            }
                        }
            });

            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void generateAndStoreKeys(String username){
        try {
            Calendar start = new GregorianCalendar();
            Calendar end = new GregorianCalendar();
            end.add(Calendar.YEAR, 20);
            KeyPairGenerator kgen = KeyPairGenerator.getInstance(Constants.KEY_ALGO, Constants.ANDROID_KEYSTORE);
            AlgorithmParameterSpec spec = new KeyPairGeneratorSpec.Builder(this)
                    .setKeySize(Constants.KEY_SIZE)
                    .setAlias(username)
                    .setSubject(new X500Principal("CN=" + username))
                    .setSerialNumber(BigInteger.valueOf(Constants.CERT_SERIAL))
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            kgen.initialize(spec);
            KeyPair kp = kgen.generateKeyPair();
            user.privateKey = kp.getPrivate();                                         // private key in a Java class (PrivateKey)
            user.publicKey = kp.getPublic();
            //X509Certificate cert = user.privateKey.
            // the corresponding public key in a Java class (PublicKey)
        }
        catch (Exception e) {
            Log.d(TAG_REGISTER, "Error creating key for user: " + username);
            Log.d(TAG_REGISTER, e.getMessage());
        }
    }

    public boolean checkUserKeyExists(String username) {
        boolean hasUserKey = false;
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(username, null);
            hasUserKey = (entry != null);
            if (hasUserKey)
                return true;
        } catch (Exception e) {
            Log.d(TAG_REGISTER, "User key already exists.");
            hasUserKey = false;
        }
        return hasUserKey;
    }

    private void writeToFileUUID(Context context, String filename, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.d("Exception", "File write failed: " + e.toString());
        }
    }

    private void sendToLoginPage() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

}

