package com.example.democart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.example.democart.Model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
}
