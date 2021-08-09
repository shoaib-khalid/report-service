package com.kalsym.report.service.model.report;

import com.kalsym.report.service.model.SettlementStatus;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@Setter
@ToString
@Table(name = "store_settlement")
public class StoreSettlement implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    private String cycle;
    private String storeId;
    private String clientId;
    private String clientName;
    private String storeName;
    private Double totalTransactionValue;
    private Double totalServiceFee;
    private Double totalCommisionFee;
    private Double totalRefund;
    private Double totalStoreShare;

    @Column(columnDefinition = "enum('PAID', 'AVAILABLE', 'RUNNING')")
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;

    private Date cycleStartDate;
    private Date cycleEndDate;

    private Date settlementDate;
    private String referenceId;

}
