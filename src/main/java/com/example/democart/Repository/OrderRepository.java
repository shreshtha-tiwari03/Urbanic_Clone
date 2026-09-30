package com.example.democart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.democart.Model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    java.util.List<Order> findByUseridOrderByCreatedAtDesc(Long userid);
}
