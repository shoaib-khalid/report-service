package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.report.ProductDailySale;
import com.kalsym.report.service.model.report.StoreDailySale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface StoreDailySalesRepository extends PagingAndSortingRepository<StoreDailySale, String>, JpaRepository<StoreDailySale, String>, JpaSpecificationExecutor<StoreDailySale> {

    /**
     * @param storeId
     * @param from
     * @param to
     * @param pageable
     * @return
     */
    Page<StoreDailySale> findByStoreIdAndDateBetween(String storeId, Date from, Date to, Pageable pageable);

    Page<StoreDailySale> findByDateBetween(Date from, Date to, Pageable pageable);

    List<StoreDailySale> findByDateBetween(Date from, Date to);


    @Procedure("inserDailySales")
    void insertDailySales();

    @Procedure("insertProductDailySales")
    void insertProductDailySales();

    @Procedure("insertStoreDailyTopProduct")
    void insertStoreDailyTopProduct();

}
