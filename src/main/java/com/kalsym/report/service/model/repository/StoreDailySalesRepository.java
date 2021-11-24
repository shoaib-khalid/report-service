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
    public Page<StoreDailySale> findByStoreIdAndDateBetween(String storeId, Date from, Date to, Pageable pageable);

    public Page<StoreDailySale> findByDateBetween(Date from, Date to, Pageable pageable);

    public List<StoreDailySale> findByDateBetween(Date from, Date to);

    public List<StoreDailySale> findByStoreId(String storeId);

    @Procedure("insertDailySales")
    public void insertDailySales();

    @Procedure("insertProductDailySales")
    public void insertProductDailySales();

    @Procedure("insertStoreDailyTopProduct")
    public void insertStoreDailyTopProduct();

}
