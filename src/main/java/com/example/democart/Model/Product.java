package com.example.democart.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
    @TableGenerator(name = "product_id_generator", table = "product_id_sequence", pkColumnName = "sequence_name", valueColumnName = "next_id", pkColumnValue = "product", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "product_id_generator")
    private Long productid;
    @Column(nullable = false)
    private String name;

    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCatogary productCatogary;
    @Column(nullable = false)
    private java.math.BigDecimal price;
    @Column(nullable = false)
    private Long qnty;
    @Column(nullable = false)
    @Builder.Default
    private String currency = "USD";
    @jakarta.persistence.ManyToOne
    private Inventory inventory;

}
