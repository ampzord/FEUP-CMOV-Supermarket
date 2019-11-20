package com.example.supermarket.supermarket.terminal.model;

import java.io.Serializable;
import java.util.UUID;

public class Product {
    public String name;
    public float cost;
    public String s_uuid;

    public Product(String name, float cost, String s_uuid) {
        this.name = name;
        this.cost = cost;
        this.s_uuid = s_uuid;
    }


}


