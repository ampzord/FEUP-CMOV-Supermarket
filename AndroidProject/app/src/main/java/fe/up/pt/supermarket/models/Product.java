package fe.up.pt.supermarket.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.UUID;

public class Product implements Parcelable, Serializable {
    private String name;
    private int euros;
    private int cents;
    private String s_uuid;

    protected Product(Parcel in) {
        name  = in.readString();
        euros = in.readInt();
        cents = in.readInt();
        s_uuid = in.readString();
    }

    public Product(String name, int euros, int cents, String s_uuid) {
        this.name = name;
        this.euros = euros;
        this.cents = cents;
        this.s_uuid = s_uuid;
    }

    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public static Creator<Product> getCREATOR() {
        return CREATOR;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(euros);
        parcel.writeInt(cents);
        parcel.writeString(s_uuid);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEuros() {
        return euros;
    }

    public void setEuros(int euros) {
        this.euros = euros;
    }

    public int getCents() {
        return cents;
    }

    public void setCents(int cents) {
        this.cents = cents;
    }

    public String getUuid() {
        return s_uuid;
    }

    public void setUuid(String s_uuid) {
        this.s_uuid = s_uuid;
    }
}


