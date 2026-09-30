package com.example.democart.Services;

import com.example.democart.Model.Cart;
import com.example.democart.Model.CartItem;
import com.example.democart.Model.Order;
import com.example.democart.Model.OrderItem;
import com.example.democart.Model.OrderStatus;
import com.example.democart.Model.Product;
import com.example.democart.Repository.CartRepository;
import com.example.democart.Repository.OrderRepository;
import com.example.democart.Repository.ProductRepository;
import com.example.democart.dto.CartResponse;
import com.example.democart.dto.OrderResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
            OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public CartResponse getCart(Long shopperId) {
        return toResponse(getOrCreateCart(shopperId));
    }

    @Transactional
    public CartResponse addItem(Long shopperId, Long productId, Long quantity) {
        requirePositive(quantity);
        Cart cart = getOrCreateCart(shopperId);
        Product product = getProduct(productId);
        if (!cart.getItems().isEmpty()
                && !cart.getItems().get(0).getProduct().getCurrency().equals(product.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "All cart items must use the same currency");
        }
        CartItem item = findItem(cart, productId);
        long updatedQuantity = quantity + (item == null ? 0 : item.getQuantity());
        checkStock(product, updatedQuantity);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(updatedQuantity);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(Long shopperId, Long productId, Long quantity) {
        requirePositive(quantity);
        Cart cart = findCart(shopperId);
        CartItem item = findItem(cart, productId);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        checkStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long shopperId, Long productId) {
        Cart cart = findCart(shopperId);
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getProductid().equals(productId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse clearCart(Long shopperId) {
        Cart cart = findCart(shopperId);
        cart.getItems().clear();
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public OrderResponse checkout(Long shopperId, String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentMethod is required");
        }
        Cart cart = findCart(shopperId);
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot checkout an empty cart");
        }

        String currency = cart.getItems().get(0).getProduct().getCurrency();
        Order order = new Order();
        order.setUserid(shopperId);
        order.setPaymentMethod(paymentMethod.trim());
        order.setOrderstatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(Instant.now());
        order.setCurrency(currency);
        order.setTotal(BigDecimal.ZERO);
        order.setItems(new ArrayList<>());

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (!currency.equals(product.getCurrency())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "All cart items must use the same currency");
            }
            checkStock(product, cartItem.getQuantity());
            product.setQnty(product.getQnty() - cartItem.getQuantity());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getProductid());
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);
            order.setTotal(
                    order.getTotal().add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))));
        }

        cart.getItems().clear();
        cartRepository.save(cart);
        return OrderResponse.from(orderRepository.save(order));
    }

    private Cart getOrCreateCart(Long shopperId) {
        if (shopperId == null || shopperId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shopperId must be positive");
        }
        return cartRepository.findByUserid(shopperId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserid(shopperId);
            cart.setItems(new ArrayList<>());
            return cartRepository.save(cart);
        });
    }

    private Cart findCart(Long shopperId) {
        if (shopperId == null || shopperId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shopperId must be positive");
        }
        return cartRepository.findByUserid(shopperId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private CartItem findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getProductid().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void requirePositive(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be greater than zero");
        }
    }

    private void checkStock(Product product, Long quantity) {
        if (product.getQnty() == null || quantity > product.getQnty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for " + product.getName());
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartLine> lines = cart.getItems().stream()
                .map(item -> new CartResponse.CartLine(
                        item.getProduct().getProductid(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();
        BigDecimal total = lines.stream().map(CartResponse.CartLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String currency = cart.getItems().isEmpty() ? "USD" : cart.getItems().get(0).getProduct().getCurrency();
        return new CartResponse(cart.getUserid(), lines, total, currency);
    }
}