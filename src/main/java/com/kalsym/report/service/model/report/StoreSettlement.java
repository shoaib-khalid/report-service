package com.kalsym.report.service.model.report;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Getter
@Setter
@ToString
@Table(name = "store_settlement")
public class StoreSettlement implements Serializable {

    @Id
    private String id;

    private String storeId;

    private String clientId;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    private String clientName;
    private String storeName;

    private Float totalTransactionValue;
    private Float totalServiceFee;
    private Float commisionFee;
    private Float totalRefund;
    private Float totaltoBePayed;

    @Column(columnDefinition = "ENUM('RUNNING', 'WITHDRAWN', 'AVAILABLE_FOR_WITHDRAW')")
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;

    public enum SettlementStatus {
        RUNNING,
        WITHDRAWN,
        AVAILABLE_FOR_WITHDRAW
    }
}
