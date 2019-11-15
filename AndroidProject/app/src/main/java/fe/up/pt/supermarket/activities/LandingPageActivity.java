package fe.up.pt.supermarket.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.security.PublicKey;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;

public class LandingPageActivity extends AppCompatActivity {

    //public static String URL = "https://localhost:3000/api";
    public static String URL = "https://192.168.1.12:3001/api"; //HOME
    //public static String URL = "https://10.227.147.104:3001/api"; //FEUP
    //public static String URL = "https://grisly-mummy-10353.herokuapp.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        setDisplayBar();

        Button register = findViewById(R.id.bt_register_page);
        Button login = findViewById(R.id.bt_login_page);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent()) return;
                sendToRegisterPage();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent()) return;
                sendToLoginPage();
            }
        });

    }

    private void sendToRegisterPage() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void sendToLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void setDisplayBar() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setIcon(R.drawable.rest_icon);
            bar.setDisplayShowHomeEnabled(true);
        }
    }


}