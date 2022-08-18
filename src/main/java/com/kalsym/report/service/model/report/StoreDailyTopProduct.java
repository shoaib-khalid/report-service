package com.kalsym.report.service.model.report;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kalsym.report.service.model.Store;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@IdClass(StoreDailyTopProductIdentity.class)
@ToString
@Table(name = "store_daily_top_product")
public class StoreDailyTopProduct implements Serializable {

    @Id
    @Temporal(TemporalType.DATE)
    private Date date;
    @Id
    private String productId;

    @Id
//    private String storeId;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storeId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Store store;

    private Integer totalOrders;
    private String name;

}
