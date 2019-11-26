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
    public PrivateKey privateKey;
    public PublicKey publicKey;
    public String certificate;
    public UUID uuid;
    public String username;
    public boolean discount;
    public Voucher selectedVoucher;
    public boolean selectedVoucherHelper;

    public User() {
        shoppingCart = new ArrayList<>();
        vouchers = new ArrayList<>();
        selectedVoucher = new Voucher();
        selectedVoucherHelper = false;
    }

    public double getTotalCost() {
        /*double total = 0;
        for (int i = 0; i < shoppingCart.size(); i++) {
            double decimal = shoppingCart.get(i).cents / 100;
            double decimal2 = Math.round(decimal * 100.0) / 100.0;
            total += shoppingCart.get(i).euros + decimal2;
            total = Math.round(total * 100.0) / 100.0;
        }
        return total;*/

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

    public double getProductCost(int i) {
        double cost = 0;
        double decimal = shoppingCart.get(i).cents / 100.0;
        cost = shoppingCart.get(i).euros + decimal;
        double cost2 = Math.round(cost * 100.0) / 100.0;
        return cost2;
    }
}
