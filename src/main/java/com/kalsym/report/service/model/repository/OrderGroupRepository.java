package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.OrderGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderGroupRepository extends PagingAndSortingRepository<OrderGroup, String>, JpaRepository<OrderGroup, String> {

//    Page<OrderGroup> findAllBy(Specification<OrderGroup> specGroupOrderDailySaleWithDatesBetween, Pageable pageable);

    Page<OrderGroup> findAll(Specification<OrderGroup> specGroupOrderDailySaleWithDatesBetween, Pageable pageable);
}
