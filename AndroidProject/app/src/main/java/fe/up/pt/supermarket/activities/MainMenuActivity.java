package fe.up.pt.supermarket.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import fe.up.pt.supermarket.R;

public class MainMenuActivity extends AppCompatActivity {
    private Button shopping;
    private Button checkVouchers; //->update Vouchers
    private Button pastTransactions;
    private Button profile; //static
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        shopping = findViewById(R.id.bt_shopping);

        shopping.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {

                    }
                });

    }
}
