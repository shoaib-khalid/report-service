package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.report.StoreDailySale;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
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

    public Page<StoreDailySale>findByDateBetween( Date from, Date to, Pageable pageable);

    public List<StoreDailySale> findByDateBetween(Date from, Date to);

    @Procedure("insertDailySales")
    public void insertDailySales();

    @Procedure("insertProductDailySales")
    public void insertProductDailySales();

    @Procedure("insertStoreDailyTopProduct")
    public void insertStoreDailyTopProduct();

}
