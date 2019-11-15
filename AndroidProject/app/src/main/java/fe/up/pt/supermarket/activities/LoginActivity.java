package fe.up.pt.supermarket.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.security.PublicKey;
import java.util.UUID;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.utils.HttpsTrustManagerUtils;
import fe.up.pt.supermarket.utils.KeyStoreUtils;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;

import static fe.up.pt.supermarket.activities.LandingPageActivity.URL;

public class LoginActivity extends AppCompatActivity {
    private Button btLogin;
    private EditText username;
    private EditText password;

    private static String TAG_LOGIN = "TAG_LOGIN";

    public static UUID USER_UUID;
    public static PublicKey SERVER_PUBLIC_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        btLogin = findViewById(R.id.bt_send_login);
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
                            getUserUUID(username.getText().toString());
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
            e.printStackTrace();
        }
    }


    public void getUserUUID(String username) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String s_uuid = preferences.getString(username, "");
        USER_UUID = UUID.fromString(s_uuid);
    }

    private void sendToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }


}
