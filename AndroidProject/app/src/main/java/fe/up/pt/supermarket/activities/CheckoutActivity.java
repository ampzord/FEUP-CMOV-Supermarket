package fe.up.pt.supermarket.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.ArrayList;

import fe.up.pt.supermarket.R;
import fe.up.pt.supermarket.models.Product;
import fe.up.pt.supermarket.utils.Constants;

public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);
    }


    private byte[] buildMessage(ArrayList<Product> ad) {
        ArrayList<Integer> sels = new ArrayList<>();
        int nitems = ad.getCount();
        for (int k = 0; k < nitems; k++)
            if (ad.getItem(k).selected)
                sels.add(ad.getItem(k).type);
        int nr = sels.size();
        ByteBuffer bb = ByteBuffer.allocate((nr+1)+ Constants.KEY_SIZE/8);
        bb.put((byte)nr);
        for (int k=0; k<nr; k++)
            bb.put(sels.get(k).byteValue());
        byte[] message = bb.array();
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(RegistrationActivity.user.username, null);
            PrivateKey pri = ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();
            Signature sg = Signature.getInstance(Constants.SIGN_ALGO);
            sg.initSign(pri);
            sg.update(message, 0, nr+1);
            int sz = sg.sign(message, nr+1, Constants.KEY_SIZE/8);
            Log.d("TAG_NFC", "Sign size = " + sz + " bytes.");
        }
        catch (Exception ex) {
            Log.d("TAG_NFC", ex.getMessage());
        }
        return message;
    }
}
