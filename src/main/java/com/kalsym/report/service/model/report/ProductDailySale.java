package com.kalsym.report.service.model.report;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kalsym.report.service.model.Customer;
import com.kalsym.report.service.model.Product;
import com.kalsym.report.service.model.Store;
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
/*    private String storeId;*/
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storeId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Store store;
    private String name;

    Long ranking;

}
