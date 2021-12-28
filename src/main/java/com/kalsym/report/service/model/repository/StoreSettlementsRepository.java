package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.SettlementStatus;
import com.kalsym.report.service.model.report.StoreSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

@Repository
public interface  StoreSettlementsRepository extends PagingAndSortingRepository<StoreSettlement, String>, JpaRepository<StoreSettlement, String>, JpaSpecificationExecutor<StoreSettlement> {

    //    /**
//     *
//     * @param storeId
//     * @param from
//     * @param to
//     * @param pageable
//     * @return
//     */
    Optional<StoreSettlement> findByStoreIdAndCycleStartDateAndCycleEndDate(String storeId, String cycleStartDate, String cycleEndDate);

    Optional<StoreSettlement>  findByReferenceIdAndSettlementStatus(String referenceId, SettlementStatus status);

    //Page<StoreSettlement> findByStoreIdAndDateBetween(@Param("storeId") String storeId, @Param("from") Date from, @Param("to") Date to, Pageable pageable);
//    Page<StoreSettlement> findByStoreIdAndDateBetween( String storeId,Date from,  Date to, Pageable pageable);
    //List<Object[]> findByStoreIdAndDate(@Param("storeId") String storeId, @Param("from") String from, @Param("to") String to);
}
