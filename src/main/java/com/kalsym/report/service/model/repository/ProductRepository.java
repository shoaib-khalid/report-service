package com.kalsym.report.service.model.repository;

import com.kalsym.report.service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findAllByStoreId(String storeId);
    Product getOne(String id);
}
