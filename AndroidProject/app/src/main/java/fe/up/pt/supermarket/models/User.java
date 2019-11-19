package fe.up.pt.supermarket.models;

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


    public User() {
        shoppingCart = new ArrayList<>();
        vouchers = new ArrayList<>();
        selectedVoucher = new Voucher();
    }

    public Float getTotalCost() {
        float total = 0;
        for (int i = 0; i < shoppingCart.size(); i++) {
            int decimal = shoppingCart.get(i).cents / 100;
            total += shoppingCart.get(i).euros + decimal;
        }
        return total;
    }

}
