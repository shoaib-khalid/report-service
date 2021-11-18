package com.kalsym.report.service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailStoreSales {


    private String storeId;
    private String merchantName;
    private String storeName;
    private double subTotal;
    private double total;
    private double serviceCharge;
    private double deliveryCharge;
    private String customerName;
    private String orderStatus;
    private String deliveryStatus;
    private double commission;

}
