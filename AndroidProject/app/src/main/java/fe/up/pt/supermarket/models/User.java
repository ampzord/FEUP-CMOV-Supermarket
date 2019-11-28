package fe.up.pt.supermarket.models;

import android.util.Log;

import java.lang.reflect.Array;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.UUID;

public class User {
    public ArrayList<Product> shoppingCart;
    public ArrayList<Voucher> vouchers;
    public ArrayList<Transaction> transactions;
    public PrivateKey privateKey;
    public PublicKey publicKey;
    public String certificate;
    public UUID uuid;
    public String username;
    public boolean discount;
    public Voucher selectedVoucher;
    public boolean selectedVoucherHelper;
    public String fullname;

    public User() {
        shoppingCart = new ArrayList<>();
        vouchers = new ArrayList<>();
        selectedVoucher = new Voucher();
        selectedVoucherHelper = false;
        transactions = new ArrayList<>();
        fullname = "";
    }

    public double getTotalCost() {
        double result = 0;
        for (int i = 0; i < shoppingCart.size(); i++) {
            Log.d("TAG_CALCULATIONS", "euros: " + shoppingCart.get(i).euros);
            Log.d("TAG_CALCULATIONS", "cents: " + shoppingCart.get(i).cents);
            double decimal_percentage = Math.round(shoppingCart.get(i).cents * 1) / 100.0;
            Log.d("TAG_CALCULATIONS", "decimal_percentage:" + decimal_percentage);
            result += shoppingCart.get(i).euros + decimal_percentage;
        }
        Log.d("TAG_CALCULATIONS", "TOTAL:" + result);
        return result;
    }

    public String getVoucherDiscountFromStringUUID(String voucher_uuid) {
        for (int i = 0; i < vouchers.size(); i++) {
            if (vouchers.get(i).uuid.toString().equals(voucher_uuid)) {
                Log.d("TAG_TRA", "Found Equal UUID from Transac Voucher.");
                return Integer.toString(vouchers.get(i).discount_percentage);
            }
        }
        return "";
    }

    public String getVoucherDiscountFromUUID(UUID voucher_uuid) {
        Log.d("TAG_TRA", "size of vouchers: " + vouchers.size());
        Log.d("TAG_TRA", "UUID argument: " + voucher_uuid.toString());
        for (int i = 0; i < vouchers.size(); i++) {
            Log.d("TAG_TRA", "Voucher i: " + vouchers.get(i).uuid.toString());
            if (vouchers.get(i).uuid.toString().equals(voucher_uuid.toString())) {
                Log.d("TAG_TRA", "UUID - Found Equal UUID from Transac Voucher.");
                return Integer.toString(vouchers.get(i).discount_percentage);
            }
        }
        return "";
    }

}
