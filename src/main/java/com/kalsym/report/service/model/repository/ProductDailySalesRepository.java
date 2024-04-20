package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.report.ProductDailySale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ProductDailySalesRepository extends PagingAndSortingRepository<ProductDailySale, String>, JpaRepository<ProductDailySale, String>, JpaSpecificationExecutor<ProductDailySale> {

    /**
     * @param storeId
     * @param from
     * @param to
     * @param pageable
     * @return
     */
    @Query(value = "SELECT *, RANK() OVER(PARTITION BY date ORDER by totalOrders)as ranking, productId , storeId , date , totalOrders, name FROM symplified.product_daily_sale ORDER by  date , ranking asc limit 10", nativeQuery = true)
    public Page<ProductDailySale> findByStoreIdAndDateBetween(String storeId, Date from, Date to, Pageable pageable);

}
