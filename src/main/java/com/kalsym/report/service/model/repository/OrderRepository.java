package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    //
//    Order getOneById(String orderId);
//
//    List<Order> findAllByStoreIdAndPaymentStatus(String storeId, String status);
//
////    @Modifying
//    @Query(value = "SELECT * FROM order WHERE storeId= :storeId AND (created BETWEEN :startDate AND :endDate) AND paymentStatus= :status", nativeQuery = true)
//    List<Order> findAllByStoreIdAndCreatedAndPaymentStatus(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("status") String status);
//

    List<Order> findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("status") String status);
    List<Order> findAllByStoreIdAndCreatedAfterAndCreatedBefore(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate);

}
