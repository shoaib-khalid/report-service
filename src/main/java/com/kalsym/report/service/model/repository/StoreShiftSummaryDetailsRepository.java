package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.StoreShiftSummaryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreShiftSummaryDetailsRepository extends JpaRepository<StoreShiftSummaryDetails, String> {

}
