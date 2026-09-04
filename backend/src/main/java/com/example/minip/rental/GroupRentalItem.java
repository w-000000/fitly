package com.example.minip.rental;

import jakarta.persistence.Embeddable;

@Embeddable
public class GroupRentalItem {
    private String category;
    private int quantity;

    protected GroupRentalItem() {}
    public GroupRentalItem(String category, int quantity) {
        this.category = category;
        this.quantity = quantity;
    }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
}
