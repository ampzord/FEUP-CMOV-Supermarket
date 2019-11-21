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
    public double result;

    public Product(String name, int euros, int cents, String s_uuid) {
        this.name = name;
        this.euros = euros;
        this.cents = cents;
        this.s_uuid = s_uuid;
        this.result = getDecimalCost();
    }

    public double getDecimalCost() {
        double cost = 0;
        double decimal = cents / 100.0;
        cost = euros + decimal;
        double cost2 = Math.round(cost * 100.0) / 100.0;
        return cost2;
    }



}


