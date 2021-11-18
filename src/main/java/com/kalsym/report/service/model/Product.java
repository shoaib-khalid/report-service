package com.kalsym.report.service.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@ToString
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;
//    private Integer stock;
    private String storeId;
    private String categoryId;
    private String status;
    private String thumbnailUrl;
    private String vendor;
    private String description;
//    private String barcode;
    private String region;
//    private String weight;
//    private String deliveryType;
//    private String itemType;
    private String seoUrl;


}
