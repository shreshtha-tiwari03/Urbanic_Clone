package com.example.democart.dto;

import com.example.democart.Model.ProductCatogary;
import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        ProductCatogary category,
        BigDecimal price,
        String currency,
        Long stockQuantity) {
}