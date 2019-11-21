package com.example.supermarket.supermarket.terminal.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

public class Transaction {
    public ArrayList<Product> products;
    public User user;
    public UUID id;
    public double totalCost;
    public int discount;
    public int productsSize;
    public Voucher voucher;
    public int hasVoucher;
    //voucher yes or no 16bits

    public Transaction() {
        id = UUID.randomUUID();
        products = new ArrayList();
        user = new User();
        voucher = new Voucher();
    }

}