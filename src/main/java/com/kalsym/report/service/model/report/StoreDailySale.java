package com.kalsym.report.service.model.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kalsym.report.service.model.Store;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@IdClass(StoreDailySaleIdentity.class)
@ToString
@Table(name = "store_daily_sale")
public class StoreDailySale implements Serializable {

    @Id
    @Temporal(TemporalType.DATE)
    private Date date;
    @Id
    private String storeId;

    private Integer totalOrders;
    private Integer successFullOrders;
    private Integer canceledOrders;
    private Double amountEarned;
    private Double commision;
    private Double totalServiceCharge;
    private Double totalAmount;
    private Double totalDeliveryFee;
    private Double totalSelfDeliveryFee;
    private Double totalAppliedDiscount;
    private Double totalDeliveryDiscount;
    //    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "storeId", insertable = false, updatable = false)
//    @Fetch(FetchMode.JOIN)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storeId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Store store;

    private String settlementReferenceId;

}
