package com.example.minip.rental;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

@Embeddable
public class GroupRentalItem {
    @Column(nullable = false, length = 30)
    private String category;
    @Column(name = "requested_quantity", nullable = false)
    private int quantity;

    protected GroupRentalItem() {}
    public GroupRentalItem(String category, int quantity) {
        this.category = category;
        this.quantity = quantity;
    }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
}
