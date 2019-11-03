package fe.up.pt.supermarket.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import fe.up.pt.supermarket.utils.HttpsTrustManager;
import fe.up.pt.supermarket.R;

import static fe.up.pt.supermarket.activities.LandingPageActivity.URL;
import static java.lang.System.out;

public class RegistrationActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_registration);

        register = findViewById(R.id.bt_send_register);
        firstName = findViewById(R.id.edit_first_name);
        lastName = findViewById(R.id.edit_last_name);
        username = findViewById(R.id.edit_username);
        password = findViewById(R.id.edit_password);
        password_conf = findViewById(R.id.edit_password_conf);
        credit_card = findViewById(R.id.credit_card);

        register.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        sendRegistrationRequest();
                    }
                });
    }

    private void sendRegistrationRequest() {
        HttpsTrustManager.allowAllSSL();
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

            //Log.d("RegistrationRequest", "URL: " + newURL);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, newURL, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResponse(JSONObject response) {
                            //Log.d("RegistrationRequest", "Entered1");
                            try {
                                String message = response.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                generateKeys();
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
                            Log.d("RegisRequest", volleyError.toString());
                            if (volleyError.networkResponse != null) {
                                Log.d("Error.Response", "Status Code Error: " + volleyError.networkResponse.statusCode);
                                Log.d("Error.Response", "Server Error Response: " + new String(volleyError.networkResponse.data));
                                Toast.makeText(getApplicationContext(), new String(volleyError.networkResponse.data), Toast.LENGTH_SHORT).show();
                            }
                        }
            });

            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateKeys() {
        try {

            /* Generate Public and Private key */
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            Key pub = kp.getPublic();
            Key pvt = kp.getPrivate();

            /* Save Public and Private keys to File */
            OutputStream out;

            /* Private key */
            out = new FileOutputStream("private.key");
            out.write(pvt.getEncoded());
            out.close();

            /* Public key */
            out = new FileOutputStream("public.pub");
            out.write(pub.getEncoded());
            out.close();

            Log.d("ASSYMETRIC KEYS", "Private key: " + pvt.getFormat());
            Log.d("ASSYMETRIC KEYS", "Public key: " + pub.getFormat());

            //-------------------------------------------

            /* Read public and private keys bites from file */

            /* Read all bytes from the private key file */
            Path path = Paths.get("private.key");
            byte[] bytes = Files.readAllBytes(path);

            /* Generate private key. */
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey pvt2 = kf.generatePrivate(ks);

            //--------------------------------

            /* Read all the public key bytes */
            Path path2 = Paths.get("public.pub");
            byte[] bytes2 = Files.readAllBytes(path2);

            /* Generate public key. */
            X509EncodedKeySpec ks2 = new X509EncodedKeySpec(bytes2);
            KeyFactory kf2 = KeyFactory.getInstance("RSA");
            PublicKey pub2 = kf2.generatePublic(ks2);

            //--------------------------------------------------

            /* Write public and private keys to txt file */

            Base64.Encoder encoder = Base64.getEncoder();

            String publickeyString = "public_key_string";
            String privatekeyString = "private_key_string";

            Writer out2 = new FileWriter(privatekeyString + ".key");
            out2.write("-----BEGIN RSA PRIVATE KEY-----\n");
            out2.write(encoder.encodeToString(pvt.getEncoded()));
            out2.write("\n-----END RSA PRIVATE KEY-----\n");
            out2.close();

            out2 = new FileWriter(publickeyString + ".pub");
            out2.write("-----BEGIN RSA PUBLIC KEY-----\n");
            out2.write(encoder.encodeToString(pub.getEncoded()));
            out2.write("\n-----END RSA PUBLIC KEY-----\n");
            out2.close();

            /*String filename = "myfile";
            String fileContents = "Hello world!";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
        catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
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

