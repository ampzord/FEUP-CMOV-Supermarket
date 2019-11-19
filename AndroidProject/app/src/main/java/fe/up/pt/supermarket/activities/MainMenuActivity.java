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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.crypto.Cipher;

import fe.up.pt.supermarket.models.Product;
import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.utils.Constants;

public class MainMenuActivity extends AppCompatActivity {
    private ImageButton scanItem;
    private Button goShopping;
    private Button checkout;
    private Button clearList;
    private Button sendKey;
    private Button tryNFC;
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
        adapter.setProductsInfo(RegistrationActivity.user.shoppingCart);

        //goShopping = findViewById(R.id.bt_shopping);
        sendKey = findViewById(R.id.sendKey);
        scanItem = findViewById(R.id.bt_scan_item);
        checkout = findViewById(R.id.checkout);
        clearList = findViewById(R.id.clearList);
        tryNFC = findViewById(R.id.tryNFC);


        scanItem.setOnClickListener((v)->scan(true));
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        tryNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] message = buildMessage();
                Intent intent = new Intent(context, NFCSendActivity.class);
                intent.putExtra("message", message);
                intent.putExtra("mime", "application/nfc.fe.up.pt.supermarket");
                startActivity(intent);
            }
        });
    }


    void sendUserPublicKeyToTerminal() {
        X509Certificate cert;

        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(RegistrationActivity.user.username, null);
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
        if (RegistrationActivity.user.discount)
            discount_int = 1;


        int n = RegistrationActivity.user.shoppingCart.size();
        //--------------------------------------------- REMOVED VOUCHER FOR TESTING
        int length = 4 + 16 + 4 + 4 + 4 + 1 + RegistrationActivity.user.shoppingCart.get(0).name.length(); //tagID, UUID, cost, voucher, discount, N, list of N
        tag = ByteBuffer.allocate(length);
        tag.putInt(Constants.tagId); //4 - tagID
        tag.putLong(RegistrationActivity.user.uuid.getMostSignificantBits()); // 8
        tag.putLong(RegistrationActivity.user.uuid.getLeastSignificantBits()); // 8 - UUID
        tag.putFloat(RegistrationActivity.user.getTotalCost()); //4 - cost
        //tag.putLong(RegistrationActivity.user.selectedVoucher.uuid.getMostSignificantBits()); // 8
        //tag.putLong(RegistrationActivity.user.selectedVoucher.uuid.getLeastSignificantBits()); // 8 - Voucher UUID
        tag.putInt(discount_int); // discount
        tag.putInt(n); // size of shoppingList
        for (int i = 0; i < n; i++) {
            tag.put((byte) RegistrationActivity.user.shoppingCart.get(i).name.length()); //name.length
            tag.put(RegistrationActivity.user.shoppingCart.get(i).name.getBytes(StandardCharsets.ISO_8859_1)); //name.getBytes()
        }


        try {
            Cipher cipher = Cipher.getInstance(Constants.ENC_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, RegistrationActivity.user.privateKey);
            encTag = cipher.doFinal(tag.array());
        }
        catch (Exception e) {
            Log.d("MAIN_MENU", "Error generating Checkout QRCode.");
        }
        Intent qrAct = new Intent(this, QRTag.class);
        qrAct.putExtra("data", encTag);
        startActivity(qrAct);
    }

    private byte[] buildMessage() {
        /*ArrayList<Integer> sels = new ArrayList<>();
        int nitems = ad.getCount();
        for (int k = 0; k < nitems; k++)
            if (ad.getItem(k).selected)
                sels.add(ad.getItem(k).type);
        int nr = sels.size();
        ByteBuffer bb = ByteBuffer.allocate((nr+1)+Constants.KEY_SIZE/8);
        bb.put((byte)nr);
        for (int k=0; k<nr; k++)
            bb.put(sels.get(k).byteValue());
        byte[] message = bb.array();*/
        /*int nr = RegistrationActivity.user.shoppingCart.size();
        ByteBuffer bb = ByteBuffer.allocate((nr+1)+Constants.KEY_SIZE/8);
        bb.put((byte)nr);
        for (int k=0; k<nr; k++)
            bb.put(toStream(RegistrationActivity.user.shoppingCart.get(k)));
        byte[] message = bb.array();
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry("checkout", null);
            PrivateKey pri = ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();
            Signature sg = Signature.getInstance(Constants.SIGN_ALGO);
            sg.initSign(pri);
            sg.update(message, 0, nr+1);
            int sz = sg.sign(message, nr+1, Constants.KEY_SIZE/8);
            Log.d("MAIN_MENU", "Sign size = " + sz + " bytes.");
        }
        catch (Exception ex) {
            Log.d("MAIN_MENU", ex.getMessage());
        }
        return message;*/
        return null;
    }

    public static byte[] toStream(Product stu) {
        // Reference for stream of bytes
        byte[] stream = null;
        // ObjectOutputStream is used to convert a Java object into OutputStream
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);) {
            oos.writeObject(stu);
            stream = baos.toByteArray();
        } catch (IOException e) {
            // Error in serialization
            e.printStackTrace();
        }
        return stream;
    }

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
        //adapter.setProductsInfo(RegistrationActivity.user.shoppingCart);
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
            cipher.init(Cipher.DECRYPT_MODE, RegistrationActivity.SERVER_CERTIFICATE);
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

        RegistrationActivity.user.shoppingCart.add(pro);
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
