package fe.up.pt.supermarket.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import fe.up.pt.supermarket.utils.HttpsTrustManagerUtils;
import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.utils.KeyStoreUtils;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;

import static fe.up.pt.supermarket.activities.LandingPageActivity.URL;

public class RegistrationActivity extends AppCompatActivity {
    private Button register;
    private EditText firstName;
    private EditText lastName;
    private EditText username;
    private EditText password;
    private EditText password_conf;
    private EditText credit_card;

    private String TAG_REGISTER = "TAG_REGISTER";

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

            String pubKey = KeyStoreUtils.generateKeys(username.getText().toString());

            jsonBody.put("public_key", pubKey);

            //Log.d("RegistrationRequest", "URL: " + newURL);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, newURL, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResponse(JSONObject response) {
                            //Log.d("RegistrationRequest", "Entered1");
                            try {
                                String server_msg_response = response.getString("message");
                                Toast.makeText(getApplicationContext(), server_msg_response, Toast.LENGTH_SHORT).show();
                                JSONObject getSth = response.getJSONObject("user");
                                Object uuid = getSth.get("uuid");
                                Log.d(TAG_REGISTER, "UUID: " + uuid.toString());
                                Log.d(TAG_REGISTER, "SERVER PUBLIC KEY: " + response.getString("server_public_key"));
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            //Log.d("RegistrationRequest", "Entered2");
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public void getJSONRequest() {
        String newURL = URL + "/register";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                newURL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("REST Response", response.toString());

                        JSONObject obj = response;
                        try {
                            String name = obj.getString("username");
                            Toast.makeText(getApplicationContext(), name,Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("REST Error Response", error.toString());
                    }
                }


        );

        requestQueue.add(objectRequest);
    }
}

