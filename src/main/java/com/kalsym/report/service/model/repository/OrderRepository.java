package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {


    List<Order> findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("status") String status);

    List<Order> findAllByStoreIdAndCreatedAfterAndCreatedBefore(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query(value = "SELECT o.id, o.storeId, c.name AS clientName, c.username, s.name AS storeName, o.total, o.created," +
            " osd.receiverName AS customerName, o.klCommission, o.deliveryCharges, o.serviceCharges, o.paymentStatus," +
            " o.completionStatus, o.subTotal FROM symplified.`order` o INNER JOIN symplified.store s INNER JOIN symplified.client c" +
            " INNER JOIN symplified.order_shipment_detail osd ON o.storeId = s.id AND s.clientId = c.id AND o.id = osd.orderId" +
            " WHERE o.storeId = :storeId AND o.created > :startDate AND o.created < :endDate AND o.paymentStatus = :status ORDER BY :sort :value", nativeQuery = true)
    List<Object[]> findAllByStoreIdAndDateRangeAndPaymentStatus(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("status") String status, @Param("sort") String sort,  @Param("value") String value);

}
