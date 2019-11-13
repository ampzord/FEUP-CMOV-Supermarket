package fe.up.pt.supermarket;

import java.util.UUID;

public class Product {

    private String name;
    private Integer euros;
    private Integer cents;
    private UUID uuid;

    public Product(String name, Integer euros, Integer cents, UUID uuid) {
        this.name = name;
        this.euros = euros;
        this.cents = cents;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEuros() {
        return euros;
    }

    public void setEuros(Integer euros) {
        this.euros = euros;
    }

    public Integer getCents() {
        return cents;
    }

    public void setCents(Integer cents) {
        this.cents = cents;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }




}
