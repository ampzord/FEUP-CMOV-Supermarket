package fe.up.pt.supermarket.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.utils.HttpsTrustManagerUtils;
import fe.up.pt.supermarket.utils.KeyStoreUtils;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;

import static fe.up.pt.supermarket.activities.LoginActivity.URL;
import static fe.up.pt.supermarket.activities.LoginActivity.hasServerKey;
import static fe.up.pt.supermarket.activities.LoginActivity.user;

public class ProfileActivity extends AppCompatActivity {

    private Button pastTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pastTransactions = findViewById(R.id.bt_past_transactions);


        pastTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent())
                    return;
                sendToPastTransactionsPage();
            }
        });

    }

    private void sendToPastTransactionsPage() {
        Intent intent = new Intent(getApplicationContext(), PastTransactionsActivity.class);
        startActivity(intent);
    }

    private void sendToPastTransactionsRequest() {
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
                        Log.d("REST Error Response", error.toString());
                    }
                }


        );

        queue.add(objectRequest);
    }


}
