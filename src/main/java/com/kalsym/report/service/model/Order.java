package com.kalsym.report.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

    private String storeId;
    private Float subTotal;
    private Float total;
    private String completionStatus;
    private String paymentStatus;
    private String customerNotes;
    private String privateAdminNotes;
    private String cartId;
    private String customerId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created;
}
