package com.kalsym.report.service.model.report;

import java.io.Serializable;
import java.util.Date;

import com.kalsym.report.service.model.Store;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeId", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private Store store;
    
    private String settlementReferenceId;

}
