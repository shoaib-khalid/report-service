package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends PagingAndSortingRepository<Order, String>, JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {


    @Query(value = "SELECT o.id,  o.storeId,  c.name AS clientName, c.username, s.name AS storeName, o.total, o.created,  osd.receiverName AS customerName, " +
            "   o.klCommission,   o.deliveryCharges,  o.storeServiceCharges, o.paymentStatus,   o.completionStatus,  o.subTotal,  o.appliedDiscount, " +
            "   o.deliveryDiscount,   o.storeVoucherDiscount,   v.voucherCode, " +
            "   o.serviceType,  o.channel, " +
            "   (SELECT COUNT(*) FROM symplified.order_item oi WHERE oi.orderId = o.id ) as noOrderItem " +
            "FROM symplified.`order` o " +
            "LEFT JOIN symplified.voucher v ON o.storeVoucherId = v.id " +
            "INNER JOIN symplified.store s INNER JOIN symplified.client c " +
            "INNER JOIN symplified.order_shipment_detail osd ON o.storeId = s.id AND s.clientId = c.id AND o.id = osd.orderId " +
            "WHERE  " +
            "   (:storeId is null or o.storeId = :storeId) " +
            "   AND  o.created > :startDate " +
            "   AND (:type is null or o.serviceType = :type)" +
            "   AND o.created < :endDate" +
            "   AND ( (serviceType='DINEIN' AND o.completionStatus ='DELIVERED_TO_CUSTOMER' AND paymentStatus='PENDING') OR (serviceType='DELIVERIN' AND paymentStatus='PAID'))" +
            "   AND s.regionCountryId = :countryCode " +
            "   AND o.channel = :channel" +
            "   ORDER BY :sort :value", nativeQuery = true)
    List<Object[]> findAllByStoreIdAndDateRangeAndPaymentStatusAndCountryCode(@Param("storeId") String storeId, @Param("startDate") String startDate,
                                                                              @Param("endDate") String endDate,
                                                                              @Param("sort") String sort, @Param("value") String value,
                                                                              @Param("countryCode") String regionCountryCode,
                                                                              @Param("type") String serviceType, @Param("channel") String channel);


    @Query(value = "SELECT " +
            "   o.id, " +
            "   o.storeId, " +
            "   c.name AS clientName, " +
            "   c.username, " +
            "   s.name AS storeName, " +
            "   o.total, " +
            "   o.created," +
            "   osd.receiverName AS customerName, " +
            "   o.klCommission, " +
            "   o.deliveryCharges, " +
            "   o.storeServiceCharges, " +
            "   o.paymentStatus," +
            "   o.completionStatus, " +
            "   o.subTotal ,  " +
            "   o.appliedDiscount , " +
            "   o.deliveryDiscount  , " +
            "   o.storeVoucherDiscount , " +
            "   v.voucherCode, " +
            "   o.serviceType, " +
            "   o.channel " +
            "FROM symplified.`order` o  " +
            "LEFT JOIN symplified.voucher v ON o.storeVoucherId = v.id " +
            "INNER JOIN symplified.store s INNER JOIN symplified.client c " +
            "INNER JOIN symplified.order_shipment_detail osd ON o.storeId = s.id AND s.clientId = c.id AND o.id = osd.orderId " +
            "WHERE " +
            "   o.created > :startDate" +
            "   AND (:type = 'null' or o.serviceType = :type)" +
            "   AND o.created < :endDate  " +
            "   AND ( (serviceType='DINEIN' AND o.completionStatus ='DELIVERED_TO_CUSTOMER' AND paymentStatus='PENDING') OR (serviceType='DELIVERIN' AND paymentStatus='PAID'))" +
            "   AND s.regionCountryId = :countryCode " +
            "   AND o.channel = :channel" +
            "   ORDER BY :sort :value", nativeQuery = true)
    List<Object[]> findAllByDateRangeAndPaymentStatusAndCountryCode(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                                                    @Param("sort") String sort,
                                                                    @Param("value") String value, @Param("countryCode") String countryCode,
                                                                    @Param("type") String serviceType, @Param("channel") String channel);

    @Query(value = "SELECT o.id, o.storeId, c.name AS clientName, c.username, s.name AS storeName, o.total, o.created," +
            " osd.receiverName AS customerName, o.klCommission, o.deliveryCharges, o.storeServiceCharges, o.paymentStatus," +
            " o.completionStatus, o.subTotal ,  o.appliedDiscount , o.deliveryDiscount  , o.storeVoucherDiscount , v.voucherCode , " +
            "   o.serviceType, " +
            "   o.channel" +
            " FROM symplified.`order` o  LEFT JOIN symplified.voucher v ON o.storeVoucherId = v.id" +
            " INNER JOIN symplified.store s INNER JOIN symplified.client c" +
            " INNER JOIN symplified.order_shipment_detail osd ON o.storeId = s.id AND s.clientId = c.id AND o.id = osd.orderId " +
            "WHERE   o.created > :startDate AND o.created < :endDate " +
            "  AND (:type is null or o.serviceType = :type)" +
            "  AND ( (serviceType='DINEIN' AND o.completionStatus ='DELIVERED_TO_CUSTOMER' AND paymentStatus='PENDING') OR (serviceType='DELIVERIN' AND paymentStatus='PAID'))" +
            "  AND o.channel = :channel  ORDER BY :sort :value", nativeQuery = true)
    List<Object[]> findAllByDateRangeAndPaymentStatusAndChannel(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                                                @Param("sort") String sort, @Param("value") String value,
                                                                @Param("type") String serviceType, @Param("channel") String channel);

    @Query(value = "SELECT o.id, o.storeId, c.name AS clientName, c.username, s.name AS storeName, o.total, o.created," +
            " osd.receiverName AS customerName, o.klCommission, o.deliveryCharges, o.storeServiceCharges, o.paymentStatus," +
            " o.completionStatus, o.subTotal ,  o.appliedDiscount , o.deliveryDiscount  , o.storeVoucherDiscount , v.voucherCode , " +
            "   o.serviceType, " +
            "   o.channel" +
            " FROM symplified.`order` o  LEFT JOIN symplified.voucher v ON o.storeVoucherId = v.id" +
            " INNER JOIN symplified.store s INNER JOIN symplified.client c" +
            " INNER JOIN symplified.order_shipment_detail osd ON o.storeId = s.id AND s.clientId = c.id AND o.id = osd.orderId " +
            "WHERE   o.created > :startDate AND o.created < :endDate " +
            "  AND (:type is null or o.serviceType = :type)" +
            "  AND ( (serviceType='DINEIN' AND o.completionStatus ='DELIVERED_TO_CUSTOMER' AND paymentStatus='PENDING') OR (serviceType='DELIVERIN' AND paymentStatus='PAID'))" +
            "  ORDER BY :sort :value", nativeQuery = true)
    List<Object[]> findAllByDateRangeAndPaymentStatus(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                                      @Param("sort") String sort, @Param("value") String value,
                                                      @Param("type") String serviceType);

    @Query(value = "SELECT o.completionStatus , COUNT(*) AS totalSales " +
            "FROM symplified.`order` o WHERE  o.created > :startDate AND o.created < :endDate AND o.storeId = :storeId AND o.serviceType = :serviceType  GROUP BY o.completionStatus", nativeQuery = true)
    List<Object[]> fineAllByStatusAndDateRange(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("serviceType") String serviceType);


    @Query(value = "SELECT o.completionStatus , COUNT(*) AS totalSales , DATE(created) " +
            "FROM symplified.`order` o WHERE  o.created > :startDate AND o.created < :endDate AND o.storeId = :storeId  AND o.serviceType = :serviceType GROUP BY o.completionStatus, DATE(created) ORDER BY  DATE(created) ASC", nativeQuery = true)
    List<Object[]> fineAllByStatusAndDateRangeAndGroupAndServiceType(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("serviceType") String serviceType);


    @Query(value = "SELECT o.completionStatus , COUNT(*) AS totalSales , DATE(created) " +
            "FROM symplified.`order` o WHERE  o.created > :startDate AND o.created < :endDate AND o.storeId = :storeId  GROUP BY o.completionStatus, DATE(created) ORDER BY  DATE(created) ASC", nativeQuery = true)
    List<Object[]> fineAllByStatusAndDateRangeAndGroup(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query(value = "SELECT COUNT(*) AS totalSales " +
            "FROM symplified.`order` o WHERE  o.created > :startDate AND o.created < :endDate AND o.storeId = :storeId AND o.serviceType = :serviceType and o.staffId = :staffId", nativeQuery = true)
    List<Object[]> fineAllByStatusAndDateRangeAndStaffId(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("serviceType") String serviceType ,  @Param("staffId") String staffId);


}
