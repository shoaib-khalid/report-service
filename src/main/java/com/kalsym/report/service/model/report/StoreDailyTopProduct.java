package com.kalsym.report.service.model.report;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
    private String storeId;

    private Integer totalOrders;
    private String name;

}
