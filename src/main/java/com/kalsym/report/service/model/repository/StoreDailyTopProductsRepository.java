package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.report.StoreDailyTopProduct;
import com.kalsym.report.service.model.report.StoreDailyTopProductIdentity;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreDailyTopProductsRepository extends JpaRepository<StoreDailyTopProduct, StoreDailyTopProductIdentity> {

    /**
     *
     * @param storeId
     * @param from
     * @param to
     * @param pageable
     * @return
     */
    public Page<StoreDailyTopProduct> findByStoreIdAndDateBetween(String storeId, Date from, Date to, Pageable pageable);

}
