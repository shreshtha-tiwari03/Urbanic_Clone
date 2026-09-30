package com.example.democart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.democart.Model.ProductCatogary;
import com.example.democart.Repository.ProductRepository;
import com.example.democart.Services.CartService;
import com.example.democart.Services.OrderService;
import com.example.democart.Services.ProductService;
import com.example.democart.dto.CartResponse;
import com.example.democart.dto.OrderResponse;
import com.example.democart.dto.ProductRequest;
import com.example.democart.dto.ProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommerceFlowTests {
    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shopperCanAddProductAndCheckout() {
        long shopperId = System.currentTimeMillis();
        ProductResponse product = productService.addProduct(new ProductRequest(
                "Flow test item", "test description", ProductCatogary.CLOTHES,
                new BigDecimal("19.99"), "USD", 5L));

        CartResponse cart = cartService.addItem(shopperId, product.productId(), 2L);
        assertEquals(new BigDecimal("39.98"), cart.total());
        assertEquals(2, cart.items().get(0).quantity());

        OrderResponse order = cartService.checkout(shopperId, "DEMO");

        assertEquals(new BigDecimal("39.98"), order.total());
        assertEquals("CONFIRMED", order.status());
        assertEquals(2, order.items().get(0).quantity());
        assertEquals(3L, productRepository.findById(product.productId()).orElseThrow().getQnty());
        assertEquals(0, cartService.getCart(shopperId).items().size());
        assertEquals(order.orderId(), orderService.getOrdersForShopper(shopperId).get(0).orderId());
    }
}