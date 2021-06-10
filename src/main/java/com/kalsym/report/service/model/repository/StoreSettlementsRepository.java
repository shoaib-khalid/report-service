package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.report.StoreSettlement;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreSettlementsRepository extends JpaRepository<StoreSettlement, String> {

    /**
     *
     * @param storeId
     * @param from
     * @param to
     * @param pageable
     * @return
     */
    @Query(value = "SELECT s FROM StoreSettlement s WHERE s.startDate >= :from AND s.endDate <= :to AND s.storeId=:storeId")
    public Page<StoreSettlement> findByStoreIdAndDateBetween(@Param("storeId") String storeId, @Param("from") Date from, @Param("to") Date to, Pageable pageable);

}
