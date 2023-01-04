package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.StoreUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreUsersRepository extends PagingAndSortingRepository<StoreUser, String>, JpaRepository<StoreUser, String> {

    StoreUser findByUsername(String userName);

    StoreUser findByUsernameOrEmail(String userName, String email);

    List<StoreUser> findByStoreId(String storeId);

    StoreUser findByUsernameAndStoreId(String username, String storeId);

    Page<StoreUser> findAll(Specification<StoreUser> specStaffReportSaleWithDatesBetween, Pageable pageable);
}
