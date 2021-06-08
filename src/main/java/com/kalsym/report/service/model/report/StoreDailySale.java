package com.kalsym.report.service.model.report;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@ToString
@Table(name = "store_daily_sale")
public class StoreDailySale implements Serializable {

    @Id
    private Date date;
    private String storeId;
    
    private Integer totalOrders;
    private Integer successFullOrders;
    private Integer canceledOrders;
    private Float amountEarned;
   
}
