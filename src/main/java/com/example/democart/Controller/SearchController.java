package com.example.democart.Controller;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.democart.Services.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.democart.dto.ProductResponse;

@RestController
@RequestMapping("/api/search")
@CrossOrigin("*")
public class SearchController {
  private final SearchService searchservice;

  public SearchController(SearchService searchservice) {
    this.searchservice = searchservice;
  }

  @GetMapping("/products/{id}")
  public ProductResponse getProduct(@PathVariable Long id) {
    return searchservice.getProduct(id);
  }

  @GetMapping("/products")
  public List<ProductResponse> searchProducts(@RequestParam(required = false) String name,
      @RequestParam(required = false) String category) {
    return searchservice.searchProducts(name, category);
  }
}
