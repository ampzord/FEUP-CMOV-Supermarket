package fe.up.pt.supermarket.models;

import java.util.UUID;

public class Product {

    private String name;
    private int euros;
    private int cents;
    private String s_uuid;

    public Product(String name, int euros, int cents, String s_uuid) {
        this.name = name;
        this.euros = euros;
        this.cents = cents;
        this.s_uuid = s_uuid;
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

    public void setUuid(String uuid) {
        this.s_uuid = s_uuid;
    }




}
