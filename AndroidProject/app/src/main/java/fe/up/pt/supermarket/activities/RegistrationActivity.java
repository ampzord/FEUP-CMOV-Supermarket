package fe.up.pt.supermarket.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import fe.up.pt.supermarket.R;

import static fe.up.pt.supermarket.activities.LandingPageActivity.URL;

public class RegistrationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //getJSONRequest();
        sendRegistrationRequest();
        //stringPostRequest();
    }

    private void sendRegistrationRequest() {
        RequestQueue queue = Volley.newRequestQueue(this);
        try {

            JSONArray array = new JSONArray();
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("fName", "Antonio");
            jsonBody.put("lName", "Pereira");
            jsonBody.put("username", "AMPZORD");
            jsonBody.put("password", "catsanddogs123");
            jsonBody.put("password_conf", "catsanddogs123");
            jsonBody.put("credit_card", "123456781234567");

            array.put(jsonBody);

            String newURL = URL + "api/register";

            JsonObjectRequest jobReq = new JsonObjectRequest(Request.Method.POST, newURL, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String message = response.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            if (volleyError.networkResponse != null) {
                                Log.d("Error.Response", "Status Code Error: " + volleyError.networkResponse.statusCode);
                                Log.d("Error.Response", "Server Error Response: " + new String(volleyError.networkResponse.data));
                                Toast.makeText(getApplicationContext(), new String(volleyError.networkResponse.data), Toast.LENGTH_SHORT).show();
                            }
                        }
            });

            queue.add(jobReq);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getJSONRequest() {
        String newURL = URL + "api/register";
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

