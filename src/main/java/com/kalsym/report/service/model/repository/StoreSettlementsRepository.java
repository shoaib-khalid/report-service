package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.report.StoreSettlement;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreSettlementsRepository extends JpaRepository<StoreSettlement, String> {

//    /**
//     *
//     * @param storeId
//     * @param from
//     * @param to
//     * @param pageable
//     * @return
//     */
    public Optional<StoreSettlement> findByStoreIdAndCycleStartDateAndCycleEndDate(String storeId, Date cycleStartDate, Date cycleEndDate);

    public Optional<StoreSettlement> findByReferenceId(String referenceId);

    //Page<StoreSettlement> findByStoreIdAndDateBetween(@Param("storeId") String storeId, @Param("from") Date from, @Param("to") Date to, Pageable pageable);
//    Page<StoreSettlement> findByStoreIdAndDateBetween( String storeId,Date from,  Date to, Pageable pageable);

    //List<Object[]> findByStoreIdAndDate(@Param("storeId") String storeId, @Param("from") String from, @Param("to") String to);
}
