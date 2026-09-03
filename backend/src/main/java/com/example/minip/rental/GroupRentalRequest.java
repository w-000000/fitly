package com.example.minip.rental;
import jakarta.persistence.*;
import java.time.*;
@Entity
public class GroupRentalRequest {
 public enum Status{RECEIVED,REVIEWING,CONFIRMED,CANCELLED}
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long customerId; private String purpose; private LocalDate startDate; private LocalDate endDate;
 private int headcount; @Column(length=1000) private String requestedItems; private String contactName; private String contactPhone;
 @Enumerated(EnumType.STRING) private Status status; private Instant createdAt;
 protected GroupRentalRequest(){}
 public GroupRentalRequest(Long customerId,String purpose,LocalDate startDate,LocalDate endDate,int headcount,String items,String name,String phone){this.customerId=customerId;this.purpose=purpose;this.startDate=startDate;this.endDate=endDate;this.headcount=headcount;this.requestedItems=items;this.contactName=name;this.contactPhone=phone;this.status=Status.RECEIVED;this.createdAt=Instant.now();}
 public Long getId(){return id;} public Long getCustomerId(){return customerId;} public String getPurpose(){return purpose;} public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;} public int getHeadcount(){return headcount;} public String getRequestedItems(){return requestedItems;} public String getContactName(){return contactName;} public String getContactPhone(){return contactPhone;} public Status getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}
