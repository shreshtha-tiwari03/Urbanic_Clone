package com.example.democart.Services;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.democart.Model.Product;
import com.example.democart.Repository.ProductRepository;
import com.example.democart.dto.ProductRequest;
import com.example.democart.dto.ProductResponse;

@Service
public class ProductService {
   private final ProductRepository productrepository;

   public ProductService(ProductRepository productrepo) {
      this.productrepository = productrepo;
   }

   public List<ProductResponse> getProducts(String name, String category) {
      List<Product> products = name == null || name.isBlank()
            ? productrepository.findAll()
            : productrepository.findByNameContainingIgnoreCase(name);
      return products.stream()
            .filter(product -> category == null || category.isBlank()
                  || product.getProductCatogary().name().equalsIgnoreCase(category))
            .map(ProductResponse::from)
            .toList();
   }

   public ProductResponse getProduct(Long productId) {
      return ProductResponse.from(findProduct(productId));
   }

   public ProductResponse addProduct(ProductRequest request) {
      validate(request);
      Product product = new Product();
      apply(product, request);
      return ProductResponse.from(productrepository.save(product));
   }

   public ProductResponse updateProduct(Long productId, ProductRequest request) {
      validate(request);
      Product product = findProduct(productId);
      apply(product, request);
      return ProductResponse.from(productrepository.save(product));
   }

   public void deleteProduct(Long productId) {
      productrepository.delete(findProduct(productId));
   }

   private Product findProduct(Long productId) {
      return productrepository.findById(productId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
   }

   private void validate(ProductRequest request) {
      if (request == null || request.name() == null || request.name().isBlank()
            || request.category() == null || request.price() == null
            || request.price().compareTo(BigDecimal.ZERO) < 0
            || request.stockQuantity() == null || request.stockQuantity() < 0) {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
               "Name, category, non-negative price, and non-negative stockQuantity are required");
      }
   }

   private void apply(Product product, ProductRequest request) {
      product.setName(request.name().trim());
      product.setDescription(request.description());
      product.setProductCatogary(request.category());
      product.setPrice(request.price());
      product.setCurrency(request.currency() == null || request.currency().isBlank()
            ? "USD"
            : request.currency().trim().toUpperCase());
      product.setQnty(request.stockQuantity());
   }
}
