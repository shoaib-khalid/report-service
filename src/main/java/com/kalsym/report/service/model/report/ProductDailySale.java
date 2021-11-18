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
@IdClass(StoreDailySaleIdentity.class)
@ToString
@Table(name = "product_daily_sale")
public class ProductDailySale implements Serializable {

    @Id
    @Temporal(TemporalType.DATE)
    private Date date;
    @Id
    private String productId;

    private Integer totalOrders;
    private String storeId;
    private String name;

}
