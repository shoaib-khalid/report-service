package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.StoreShiftSummary;
import com.kalsym.report.service.model.StoreUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface StoreShiftSummaryRepository extends JpaRepository<StoreShiftSummary, String> {

    @Query(value = "SELECT id, created FROM `order` "
            + "WHERE staffId=:staffId "
            + "AND isShiftEnd=0 "
            + "AND created > DATE_SUB(NOW(),INTERVAL 24 HOUR)"
            + "ORDER BY created LIMIT 1"
            , nativeQuery = true)
    List<Object[]> getFirstOrder(@Param("staffId") String staffId);

    @Query(value = "SELECT id, created FROM `order` "
            + "WHERE staffId=:staffId "
            + "AND isShiftEnd=0 "
            + "AND created > DATE_SUB(NOW(),INTERVAL 24 HOUR)"
            + "ORDER BY created DESC LIMIT 1"
            , nativeQuery = true)
    List<Object[]> getLastOrder(@Param("staffId") String staffId);

    @Query(value = "SELECT SUM(total), paymentChannel FROM `order` "
            + "WHERE staffId=:staffId "
            + "AND isShiftEnd=0 "
            + "AND created > DATE_SUB(NOW(),INTERVAL 24 HOUR)"
            + "GROUP BY paymentChannel"
            , nativeQuery = true)
    List<Object[]> getOrderSummary(@Param("staffId") String staffId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE `order` "
            + "SET isShiftEnd=1 "
            + "WHERE staffId=:staffId "
            + "AND isShiftEnd=0 "
            + "AND created > DATE_SUB(NOW(),INTERVAL 24 HOUR)", nativeQuery = true)
    void UpdateOrderClose(
            @Param("staffId") String staffId
    );



    Page<StoreShiftSummary> findAll(Specification<StoreShiftSummary> specStaffReportSaleWithDatesBetween, Pageable pageable);

}
