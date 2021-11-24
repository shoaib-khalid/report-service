package com.kalsym.report.service.model.report;

import java.io.Serializable;
import java.util.Date;

import com.kalsym.report.service.model.Customer;
import com.kalsym.report.service.model.Product;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

@Entity
@Getter
@Setter
//@IdClass(StoreDailySaleIdentity.class)
@ToString
@Table(name = "product_daily_sale")
public class ProductDailySale implements Serializable {

    @Id

    @Temporal(TemporalType.DATE)
    private Date date;

    private String productId;
    private Integer totalOrders;
    private String storeId;
    private String name;

}
