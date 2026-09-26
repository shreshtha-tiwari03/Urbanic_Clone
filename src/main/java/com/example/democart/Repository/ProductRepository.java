package com.example.democart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.democart.Model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
