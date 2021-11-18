package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, String> {

    List<ProductInventory> findAllByProductIdAndQuantityGreaterThanEqualAndQuantityLessThanEqual(String productId, Integer min, Integer max);
}
