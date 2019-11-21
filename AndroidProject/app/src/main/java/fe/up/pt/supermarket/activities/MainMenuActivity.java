package fe.up.pt.supermarket.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.Cipher;

import fe.up.pt.supermarket.adapter.ProductAdapter;
import fe.up.pt.supermarket.models.Product;
import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.utils.Constants;
import fe.up.pt.supermarket.utils.MultipleClicksUtils;
import fe.up.pt.supermarket.utils.NFCSendActivity;
import fe.up.pt.supermarket.utils.QRTag;

public class MainMenuActivity extends AppCompatActivity {
    private ImageButton scanItem;
    private Button goShopping;
    private Button checkout_button;
    private Button clearList;
    private Button sendKey;
    private TextView message;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    final Context context = this;

    //public static User user;

    ProductAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        /*if(savedInstanceState == null) {
            RegistrationActivity.user.shoppingCart = new ArrayList<>();
        }
        else {
            RegistrationActivity.user.shoppingCart = savedInstanceState.getParcelableArrayList("shoppingCart");
        }*/

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new ProductAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setProductsInfo(LoginActivity.user.shoppingCart);

        //goShopping = findViewById(R.id.bt_shopping);
        sendKey = findViewById(R.id.sendKey);
        scanItem = findViewById(R.id.bt_scan_item);
        checkout_button = findViewById(R.id.checkout);
        clearList = findViewById(R.id.clearList);


        scanItem.setOnClickListener((v)->scan(true));
        checkout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MultipleClicksUtils.prevent())
                    return;
                generateCheckoutTag();
            }
        });
        clearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        });
        sendKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserPublicKeyToTerminal();
            }
        });

    }


    void sendUserPublicKeyToTerminal() {
        X509Certificate cert;

        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(LoginActivity.user.username, null);
            if (entry != null) {
                cert = (X509Certificate) ((KeyStore.PrivateKeyEntry) entry).getCertificate();
                //tvKey.setText(cert.toString());
                Intent intent = new Intent(MainMenuActivity.this, NFCSendActivity.class);
                intent.putExtra("message", cert.getEncoded());
                intent.putExtra("mime", "application/nfc.fe.up.pt.pubkeyforterminal");
                startActivity(intent);
            }
        }
        catch (Exception e) {
            //tvNoKey.setText(e.getMessage());
            Log.d("MAIN_MENU", "Error loading user public key to send to Terminal.");
        }
    }


    void generateCheckoutTag() {
        //String name = edName.getText().toString();

        /*if (!hasKey || edUUID.getText().toString().length() == 0 || name.length() == 0 ||
                edEuros.getText().toString().length() == 0 || edCents.getText().toString().length() == 0) {
            tvNoKey.setText(R.string.msg_empty);
            return;
        }*/
        /*int len = 4 + 16 + 4 + 4 + 1 + edName.getText().toString().length();
        int l = edName.getText().toString().length();
        if (l > 35)
            name = edName.getText().toString().substring(0, 35);
        tag = ByteBuffer.allocate(len);
        tag.putInt(Constants.tagId); //4 - tagID
        tag.putLong(productUUID.getMostSignificantBits()); //16 - UUID
        tag.putLong(productUUID.getLeastSignificantBits()); //16 - UUID
        tag.putInt(Integer.parseInt(edEuros.getText().toString())); //4 - euros
        tag.putInt(Integer.parseInt(edCents.getText().toString())); //4 - cents
        tag.put((byte)name.length()); //name
        tag.put(name.getBytes(StandardCharsets.ISO_8859_1)); //name*/

        //


        /*

                - id
                - price
                - UUID (transmitted in registration)
                - possibly ONE VOUCHER ID (selected by the user)
                - possibility to discount the amount accumulated so far (BOOLEAN)
                - N - number of products to read
                - list of N products (max 10 items)
         */

        byte[] encTag = new byte[0];
        ByteBuffer tag;
        int discount_int = 0;
        if (LoginActivity.user.discount)
            discount_int = 1;

        UUID qrCodeUUID = UUID.randomUUID();
        int n = LoginActivity.user.shoppingCart.size();
        //--------------------------------------------- REMOVED VOUCHER FOR TESTING
        int length = 16 + 16 + 4 + 4 + 4; //tagID, qrcodeUUID, userUUID, cost, (voucher), discount, N
        for (int i = 0; i < n; i++) {
            //length += 16 + 4 + 1 + LoginActivity.user.shoppingCart.get(i).name.length(); //cost float
            length += 4 + 1 + LoginActivity.user.shoppingCart.get(i).name.length(); //cost float

        }
        for (int i = 0; i < n; i++) {
            //length += 16 + 4 + 1 + LoginActivity.user.shoppingCart.get(i).name.length(); //cost float
            //length += 16;
            //length += 4 + 1 + LoginActivity.user.shoppingCart.get(i).name.length(); //cost float
            length += 4 + 1 + LoginActivity.user.shoppingCart.get(i).name.length(); //cost float
        }
        for (int i = 0; i < n; i++) {
        }
        try {
            tag = ByteBuffer.allocate(length);
            //tag.putInt(Constants.tagId); //4 - tagID
            tag.putLong(qrCodeUUID.getMostSignificantBits()); // 8
            tag.putLong(qrCodeUUID.getLeastSignificantBits()); // 8 - TRANSACTION UUID
            tag.putLong(LoginActivity.user.uuid.getMostSignificantBits()); // 8
            tag.putLong(LoginActivity.user.uuid.getLeastSignificantBits()); // 8 - User UUID
            tag.putFloat(LoginActivity.user.getTotalCost()); //4 - cost

        /*tag.putLong(LoginActivity.user.selectedVoucher.uuid.getMostSignificantBits()); // 8
        tag.putLong(LoginActivity.user.selectedVoucher.uuid.getLeastSignificantBits()); // 8 - Voucher UUID*/

            tag.putInt(discount_int); //4 discount
            tag.putInt(n); //4 size of shoppingList
            for (int i = 0; i < n; i++) {
            /*UUID uuid_pro = UUID.fromString(LoginActivity.user.shoppingCart.get(i).s_uuid);
            tag.putLong(uuid_pro.getMostSignificantBits()); // 8
            tag.putLong(uuid_pro.getLeastSignificantBits()); // 8 - UUID of product*/

                tag.putFloat(LoginActivity.user.shoppingCart.get(i).getDecimalCost()); //4 - cost 20,75
                tag.put((byte) LoginActivity.user.shoppingCart.get(i).name.length()); //1 - name.length
                tag.put(LoginActivity.user.shoppingCart.get(i).name.getBytes(StandardCharsets.ISO_8859_1)); //name.getBytes()

            }

            tag.rewind();

            // print the ByteBuffer
            Log.d("MAIN_MENU", "Original ByteBuffer:  "
                    + Arrays.toString(tag.array()));


            //Log.d("MAIN_MENU", "UserPrivateKey: " + LoginActivity.user.privateKey.toString());
            Cipher cipher = Cipher.getInstance(Constants.ENC_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, LoginActivity.user.privateKey);
            encTag = cipher.doFinal(tag.array());
        }
        catch (IllegalArgumentException e) {
            Log.d("MAIN_MENU", "IllegalArgumentException catched");
        }

        catch (ReadOnlyBufferException e) {
            Log.d("MAIN_MENU", "ReadOnlyBufferException catched");
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Error Generating Checkout QRCode.", Toast.LENGTH_SHORT).show();
            Log.d("MAIN_MENU", "Error Generating Checkout QRCode.");
        }

        Intent qrAct = new Intent(this, QRTag.class);
        qrAct.putExtra("data", encTag);
        startActivity(qrAct);
    }

    /*

        @Override
    public void onBackPressed() {
        if(homepageBinding.fab.isExpanded())
            homepageBinding.fab.setExpanded(false);
        else if(filterBottomSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED)
            filterBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        else
            super.onBackPressed();
     */

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        //bundle.putCharSequence("Message", message.getText());
        //bundle.putParcelableArrayList("shoppingCart", RegistrationActivity.user.shoppingCart);
        //adapter.setProductsInfo(RegistrationActivity.user.shoppingCart);
    }

    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        //ArrayList<Product> getShoppingCart = bundle.getSerializable("ShoppingCart");
        //RegistrationActivity.user.shoppingCart = bundle.getParcelableArrayList("shoppingCart");
        //adapter.setProductsInfo(RegistrationActivity.user.shoppingCart);
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.setProductsInfo(LoginActivity.user.shoppingCart);
        adapter.notifyDataSetChanged();
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
            cipher.init(Cipher.DECRYPT_MODE, LoginActivity.SERVER_CERTIFICATE);
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
        Log.d("QRCODE", "Name: " + pro.name);
        Log.d("QRCODE", "euros: " + pro.euros);
        Log.d("QRCODE", "cents: " + pro.cents);
        Log.d("QRCODE", "UUID: " + pro.s_uuid);

        LoginActivity.user.shoppingCart.add(pro);
        adapter.notifyDataSetChanged();
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
