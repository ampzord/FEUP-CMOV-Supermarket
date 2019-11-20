package com.example.supermarket.supermarket.terminal.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

public class Transaction {
    public ArrayList<Product> products;
    public User user;
    public UUID id;
    public float totalCost;
    public int discount;
    //voucher yes or no 16bits

    public Transaction() {
        products = new ArrayList();
        user = new User();
    }

}
