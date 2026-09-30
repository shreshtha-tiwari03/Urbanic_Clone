package com.example.democart.dto;

import com.example.democart.Model.Product;
import java.math.BigDecimal;

public record ProductResponse(
        Long productId,
        String name,
        String description,
        String category,
        BigDecimal price,
        String currency,
        Long stockQuantity) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductid(),
                product.getName(),
                product.getDescription(),
                product.getProductCatogary().name(),
                product.getPrice(),
                product.getCurrency(),
                product.getQnty());
    }
}