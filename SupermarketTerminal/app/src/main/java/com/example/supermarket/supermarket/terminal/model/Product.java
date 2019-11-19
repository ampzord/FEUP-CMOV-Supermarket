package com.example.supermarket.supermarket.terminal.model;

import java.io.Serializable;

public class Product implements Serializable {
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

    public String toString() {
        return "Name: " + name + " Price: " + euros + "," + cents;
    }

}


