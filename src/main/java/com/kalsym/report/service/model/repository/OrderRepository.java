package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    
    

    List<Order> findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("status") String status);
    List<Order> findAllByStoreIdAndCreatedAfterAndCreatedBefore(@Param("storeId") String storeId, @Param("startDate") String startDate, @Param("endDate") String endDate);

}
