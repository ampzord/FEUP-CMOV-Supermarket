package fe.up.pt.supermarket.models;

import java.util.UUID;

public class Transaction {
    public double totalCost;
    public UUID uuid;
    public int transactionSize;
    public Boolean usedDiscount;
    public String voucher_transac_uuid;
    public String voucherUsedDiscount;

    public Transaction() {
        voucher_transac_uuid = "";
        voucherUsedDiscount = "Not Used";
    }
}
