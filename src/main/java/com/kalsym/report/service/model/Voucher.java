package com.kalsym.report.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kalsym.report.service.service.enums.DiscountCalculationType;
import com.kalsym.report.service.service.enums.VoucherDiscountType;
import com.kalsym.report.service.service.enums.VoucherStatus;
import com.kalsym.report.service.service.enums.VoucherType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Table(name = "voucher")
@NoArgsConstructor
public class Voucher implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;
    private String storeId;

    @Enumerated(EnumType.STRING)
    private VoucherType voucherType;

    @Enumerated(EnumType.STRING)
    private VoucherDiscountType discountType;

    @Enumerated(EnumType.STRING)
    private DiscountCalculationType calculationType;

}

