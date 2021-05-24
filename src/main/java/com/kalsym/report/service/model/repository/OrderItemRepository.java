package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findAllByOrderId (String orderId);

    @Query(value = "SELECT ot.productId, count(productId)  FROM symplified.order_item ot INNER JOIN symplified.order o ON o.id = ot.orderId WHERE created > :date1" +
            " AND created < :date2 AND storeId = :storeId and o.paymentStatus= :status GROUP BY ot.productId ORDER BY count(ot.productId) DESC LIMIT :limit ;", nativeQuery = true)
//    @Query(value = "SELECT ot.productId, count(productId)  FROM symplified.order_item ot INNER JOIN symplified.order o ON o.id = ot.orderId WHERE created > :date1" +
//            " AND created < :date2  GROUP BY ot.productId ORDER BY count(ot.productId) DESC LIMIT :limit ;", nativeQuery = true)
    List<Object[]> findAllByTopSaleProduct(@Param("date1") String date1, @Param("date2") String date2, @Param("storeId") String storeId, @Param("status") String status, @Param("limit") Integer limit);
//    List<Object[]> findAllByTopSaleProduct(@Param("date1") String date1, @Param("date2") String date2, @Param("limit") Integer limit);
}
