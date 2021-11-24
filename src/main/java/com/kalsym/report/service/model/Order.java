package com.kalsym.report.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@Table(name = "order")
public class Order implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;



    private Float subTotal;
    private Float total;
    private String completionStatus;
    private String paymentStatus;
    private String customerNotes;
    private String privateAdminNotes;
    private String cartId;
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "customerId", insertable = false, updatable = false)
//    @Fetch(FetchMode.JOIN)
//    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storeId", insertable = false, updatable = false)
    private Store store;
    
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created;
}
