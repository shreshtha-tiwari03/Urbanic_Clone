package com.example.democart.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {
    @Id
    private Long productid;
    @Column(nullable = false)
    private String name;

    private String Description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCatogary productCatogary;
    @Column(nullable = false)
    private Long price;
    @Column(nullable = false)
    private Long qnty;
    @Column(nullable = false)
    private Long currency;
    @ManyToOne
    private Cart cart;

    @ManyToOne
    private Inventory inventory;

}
