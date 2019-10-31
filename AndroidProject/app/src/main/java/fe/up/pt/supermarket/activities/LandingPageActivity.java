package fe.up.pt.supermarket.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import fe.up.pt.supermarket.R;

public class LandingPageActivity extends AppCompatActivity {

    public static String URL = "http://10.227.157.174:3000/";

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
                sendToRegisterPage(view);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToLoginPage(view);
            }
        });
    }

    private void sendToRegisterPage(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void sendToLoginPage(View view) {
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