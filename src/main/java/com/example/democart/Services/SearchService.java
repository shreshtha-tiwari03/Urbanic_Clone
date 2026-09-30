package com.example.democart.Services;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.democart.Model.Product;
import com.example.democart.Repository.ProductRepository;
import com.example.democart.dto.ProductResponse;

@Service
public class SearchService {
      private final ProductRepository productrepository;

      public SearchService(ProductRepository productrepo) {
            this.productrepository = productrepo;
      }

      public ProductResponse getProduct(Long productid) {
            return productrepository.findById(productid)
                        .map(ProductResponse::from)
                        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                                    org.springframework.http.HttpStatus.NOT_FOUND, "Product not found"));
      }

      public List<ProductResponse> searchProducts(String name, String category) {
            List<Product> products = name == null || name.isBlank()
                        ? productrepository.findAll()
                        : productrepository.findByNameContainingIgnoreCase(name);
            return products.stream()
                        .filter(product -> category == null || category.isBlank()
                                    || product.getProductCatogary().name().equalsIgnoreCase(category))
                        .map(ProductResponse::from)
                        .toList();
      }
}
