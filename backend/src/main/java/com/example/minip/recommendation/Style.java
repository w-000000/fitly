package com.example.minip.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "style", schema = "public")
public class Style {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "style_id")
    private Long id;

    @Column(name = "style_name", nullable = false, unique = true, length = 50)
    private String name;

    protected Style() {
    }

    public Style(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
