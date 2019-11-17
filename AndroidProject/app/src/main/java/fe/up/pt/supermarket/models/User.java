package fe.up.pt.supermarket.models;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.UUID;

public class User {
    public ArrayList<Product> shoppingCart;
    public PrivateKey privateKey;
    public PublicKey publicKey;
    public String certificate;
    public UUID uuid;
    public String username;


    public User() {
    }

}
