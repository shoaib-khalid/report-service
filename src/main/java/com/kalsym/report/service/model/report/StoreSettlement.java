package com.kalsym.report.service.model.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kalsym.report.service.model.SettlementStatus;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

import com.kalsym.report.service.model.Store;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
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

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storeId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Store store;

    private String clientId;
    private String clientName;
    private String storeName;

    private Double totalTransactionValue;
    private Double totalServiceFee;
    private Double totalCommisionFee;
    private Double totalDeliveryFee;
    private Double totalSelfDeliveryFee;
    private Double totalAppliedDiscount;
    private Double totalDeliveryDiscount;
    private Double totalRefund;
    private Double totalStoreShare;

    @Column(columnDefinition = "enum('PAID', 'AVAILABLE', 'RUNNING')")
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;

    private String cycleStartDate;
    private String cycleEndDate;

    private String settlementDate;
    private String referenceId;

}
