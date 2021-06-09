package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.report.StoreDailySale;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreDailySalesRepository extends JpaRepository<StoreDailySale, String> {

    /**
     *
     * @param storeId
     * @param from
     * @param to
     * @param pageable
     * @return
     */
    public Page<StoreDailySale> findByStoreIdAndDateBetween(String storeId, Date from, Date to, Pageable pageable);

}
