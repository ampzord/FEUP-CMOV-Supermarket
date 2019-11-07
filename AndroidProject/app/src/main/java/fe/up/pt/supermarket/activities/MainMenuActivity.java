package fe.up.pt.supermarket.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.activities.ScanQRActivity;

public class MainMenuActivity extends AppCompatActivity {
    private Button scanItem;
    private TextView text;

    private Button checkVouchers; //->update Vouchers
    private Button pastTransactions;
    private Button profile; //static
    private Button logout;

    public static String hello = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        scanItem = findViewById(R.id.bt_shopping);
        text = findViewById(R.id.answer);

        text.setText(hello);

        scanItem.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ScanQRActivity.class);
                        startActivity(intent);
                    }
                });

    }
}
