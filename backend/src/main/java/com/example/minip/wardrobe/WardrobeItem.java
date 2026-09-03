package com.example.minip.wardrobe;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class WardrobeItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long customerId;
    private String name;
    private String category;
    private String color;
    private String season;
    private String style;
    private String imageFilename;
    private String imageContentType;
    @Column(columnDefinition="bytea") private byte[] imageData;
    private Instant createdAt;
    private Instant updatedAt;
    protected WardrobeItem() {}
    public WardrobeItem(Long customerId, String name, String category, String color, String season,
                        String style, String filename, String contentType, byte[] imageData) {
        this.customerId=customerId; this.name=name; this.category=category; this.color=color;
        this.season=season; this.style=style; this.imageFilename=filename;
        this.imageContentType=contentType; this.imageData=imageData;
        this.createdAt=Instant.now(); this.updatedAt=this.createdAt;
    }
    public void update(String name,String category,String color,String season,String style) {
        if(name!=null)this.name=name; if(category!=null)this.category=category; if(color!=null)this.color=color;
        if(season!=null)this.season=season; if(style!=null)this.style=style; this.updatedAt=Instant.now();
    }
    public Long getId(){return id;} public Long getCustomerId(){return customerId;} public String getName(){return name;}
    public String getCategory(){return category;} public String getColor(){return color;} public String getSeason(){return season;}
    public String getStyle(){return style;} public String getImageFilename(){return imageFilename;}
    public String getImageContentType(){return imageContentType;} public byte[] getImageData(){return imageData;}
    public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
