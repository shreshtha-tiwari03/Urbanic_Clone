package com.example.democart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.democart.Model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
    java.util.Optional<Cart> findByUserid(Long userid);
}
