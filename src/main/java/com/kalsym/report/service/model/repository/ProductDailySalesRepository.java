package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.report.ProductDailySale;
import com.kalsym.report.service.model.report.ProductDailySaleIdentity;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDailySalesRepository extends JpaRepository<ProductDailySale, ProductDailySaleIdentity> {

    /**
     *
     * @param storeId
     * @param from
     * @param to
     * @param pageable
     * @return
     */
    public Page<ProductDailySale> findByStoreIdAndDateBetween(String storeId, Date from, Date to, Pageable pageable);

}
