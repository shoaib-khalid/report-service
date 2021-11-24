package com.kalsym.report.service.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String storeId;
    @JsonIgnore
    private String categoryId;
    @JsonIgnore
    private String status;
    @JsonIgnore
    private String thumbnailUrl;
    @JsonIgnore
    private String vendor;
    @JsonIgnore
    private String description;
    //    private String barcode;
    @JsonIgnore
    private String region;
    //    private String weight;
//    private String deliveryType;
//    private String itemType;
    @JsonIgnore
    private String seoUrl;


}
