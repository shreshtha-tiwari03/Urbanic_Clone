package com.example.democart.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.democart.Services.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping ("api/Search/")
@CrossOrigin("*")
public class SearchController {
 @Autowired 
 public SearchService searchservice;
 @GetMapping("/{id}")
 public ResponseEntity<Product> getProduct(@PathVariable Long id ) {
    optional<Product>product=Optional.ofNullable(reviewService.getproduct(id));

      return Product;
                
	  
 }
 
 

 

}
