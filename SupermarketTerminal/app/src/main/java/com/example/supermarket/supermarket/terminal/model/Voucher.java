package com.example.supermarket.supermarket.terminal.model;

import java.util.UUID;

public class Voucher {
    public UUID uuid;
    public int discount_percentage;
    public boolean used = false;

    public Voucher() {}

    public Voucher(UUID uuid, int discount_percentage) {
        this.uuid = uuid;
        this.discount_percentage = discount_percentage;
    }

    public String toString()
    {
        return("Voucher " + discount_percentage + "%");
    }
}
