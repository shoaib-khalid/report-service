package com.kalsym.report.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String orderId;
    private String productId;
    private Float price;
    private Float productPrice;
    private Float weight;
    @JsonProperty("SKU")
    private String SKU;
    private int quantity;
    private String itemCode;
    private String productName;
    private String specialInstruction;
    private String productVariant;
    private String discountId;
    private Float normalPrice;
    private String discountLabel;
    private String status;
    private Integer originalQuantity;
}