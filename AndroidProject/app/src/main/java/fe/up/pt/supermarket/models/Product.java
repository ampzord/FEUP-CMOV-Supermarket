package fe.up.pt.supermarket.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.UUID;

public class Product {
    public String name;
    public int euros;
    public int cents;
    public String s_uuid;


    public Product(String name, int euros, int cents, String s_uuid) {
        this.name = name;
        this.euros = euros;
        this.cents = cents;
        this.s_uuid = s_uuid;
    }

    public Float getDecimalCost() {
        float cost;
        int decimal = cents / 100;
        cost = euros + decimal;
        return cost;
    }

}


