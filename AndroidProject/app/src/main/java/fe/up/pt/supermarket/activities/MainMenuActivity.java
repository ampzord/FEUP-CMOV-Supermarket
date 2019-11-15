package fe.up.pt.supermarket.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.Cipher;

import fe.up.pt.supermarket.models.Product;
import fe.up.pt.supermarket.R;

public class MainMenuActivity extends AppCompatActivity {
    private ImageButton scanItem;
    private Button goShopping;
    private TextView message;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    public static ArrayList<Product> shoppingCart;
    ProductAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        if(savedInstanceState == null) {
            shoppingCart = new ArrayList<>();
        }
        else {
            shoppingCart = savedInstanceState.getParcelableArrayList("shoppingCart");
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new ProductAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.setProductsInfo(MainMenuActivity.shoppingCart);

        //goShopping = findViewById(R.id.bt_shopping);
        scanItem = findViewById(R.id.bt_scan_item);


        scanItem.setOnClickListener((v)->scan(true));
        //goShopping.setOnClickListener((v)->sendToShopping());
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        //bundle.putCharSequence("Message", message.getText());
        bundle.putParcelableArrayList("shoppingCart", shoppingCart);
        adapter.setProductsInfo(MainMenuActivity.shoppingCart);
    }

    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        //ArrayList<Product> getShoppingCart = bundle.getSerializable("ShoppingCart");
        shoppingCart = bundle.getParcelableArrayList("shoppingCart");
        adapter.setProductsInfo(MainMenuActivity.shoppingCart);
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.setProductsInfo(MainMenuActivity.shoppingCart);
    }

    public void scan(boolean qrcode) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        }
        catch (ActivityNotFoundException anfe) {
            showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private static AlertDialog showDialog(final AppCompatActivity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, (dialogInterface, i) -> {
            Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            act.startActivity(intent);
        });
        downloadDialog.setNegativeButton(buttonNo, null);
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                if (contents != null)
                    decodeAndShow(contents.getBytes(StandardCharsets.ISO_8859_1));
            }
        }
    }

    private void decodeAndShow(byte[] encTag) {
        byte[] clearTag;

        try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, RegistrationActivity.pub);
            clearTag = cipher.doFinal(encTag);
        }
        catch (Exception e) {
            Log.d("QRCODE", "Error doing cipher in qrcode.");
            return;
        }
        ByteBuffer tag = ByteBuffer.wrap(clearTag);
        int tId = tag.getInt();
        long most = tag.getLong();
        long less = tag.getLong();
        UUID id = new UUID(most, less);
        int euros = tag.getInt();
        int cents = tag.getInt();
        byte l = tag.get();
        byte[] bName = new byte[l];
        tag.get(bName);
        String name = new String(bName, StandardCharsets.ISO_8859_1);

        int tagId = 0x41636D65;
        String text = "Read Tag (" + clearTag.length + "):\n" + byteArrayToHex(clearTag) + "\n\n" +
                ((tId==tagId)?"correct":"wrong") + "\n" +
                "ID: " + id.toString() + "\n" +
                "Name: " + name + "\n" +
                "Price: €" + euros + "." + cents;
        //String name, Integer euros, Integer cents, UUID uuid
        //UUID uuidProduct = UUID.fromString(id.toString());
        Product pro = new Product(name, euros, cents, id.toString());
        Log.d("QRCODE", "Name: " + pro.getName());
        Log.d("QRCODE", "euros: " + pro.getEuros());
        Log.d("QRCODE", "cents: " + pro.getCents());
        Log.d("QRCODE", "UUID: " + pro.getUuid().toString());

        shoppingCart.add(pro);
        //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
        //message.setText(name);
    }

    String byteArrayToHex(byte[] ba) {
        StringBuilder sb = new StringBuilder(ba.length * 2);
        for(byte b: ba)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

}
