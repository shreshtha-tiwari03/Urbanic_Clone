package com.example.democart.dto;

import com.example.democart.Model.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long shopperId,
        String paymentMethod,
        String status,
        BigDecimal total,
        String currency,
        Instant createdAt,
        List<OrderLine> items) {

    public static OrderResponse from(Order order) {
        String currency = order.getItems().isEmpty() ? "USD" : order.getCurrency();
        return new OrderResponse(
                order.getOrderid(),
                order.getUserid(),
                order.getPaymentMethod(),
                order.getOrderstatus().name(),
                order.getTotal(),
                currency,
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderLine(item.getProductId(), item.getProductName(), item.getUnitPrice(),
                                item.getQuantity()))
                        .toList());
    }

    public record OrderLine(Long productId, String productName, BigDecimal unitPrice, Long quantity) {
    }
}