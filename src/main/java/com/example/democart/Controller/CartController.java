package com.example.democart.Controller;

import com.example.democart.Services.CartService;
import com.example.democart.dto.CartItemRequest;
import com.example.democart.dto.CartResponse;
import com.example.democart.dto.CheckoutRequest;
import com.example.democart.dto.OrderResponse;
import com.example.democart.dto.QuantityRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/carts/{shopperId}")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(@PathVariable Long shopperId) {
        return cartService.getCart(shopperId);
    }

    @PostMapping("/items")
    public CartResponse addItem(@PathVariable Long shopperId, @RequestBody CartItemRequest request) {
        return cartService.addItem(shopperId, request.productId(), request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(@PathVariable Long shopperId, @PathVariable Long productId,
            @RequestBody QuantityRequest request) {
        return cartService.updateItem(shopperId, productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@PathVariable Long shopperId, @PathVariable Long productId) {
        return cartService.removeItem(shopperId, productId);
    }

    @DeleteMapping
    public CartResponse clearCart(@PathVariable Long shopperId) {
        return cartService.clearCart(shopperId);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@PathVariable Long shopperId, @RequestBody CheckoutRequest request) {
        return cartService.checkout(shopperId, request.paymentMethod());
    }
}