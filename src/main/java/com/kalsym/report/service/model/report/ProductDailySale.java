package com.kalsym.report.service.model.report;

import java.io.Serializable;
import java.util.Date;

import com.kalsym.report.service.model.Customer;
import com.kalsym.report.service.model.Product;
import lombok.Getter;
import javax.persistence.Id;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

@Entity
@Getter
@Setter
@IdClass(StoreDailyTopProductIdentity.class)
@ToString
@Table(name = "product_daily_sale")
@NoArgsConstructor
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
