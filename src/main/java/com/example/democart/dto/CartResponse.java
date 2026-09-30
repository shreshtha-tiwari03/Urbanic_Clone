package com.example.democart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long shopperId, List<CartLine> items, BigDecimal total, String currency) {
    public record CartLine(Long productId, String name, BigDecimal unitPrice, Long quantity, BigDecimal lineTotal) {
    }
}