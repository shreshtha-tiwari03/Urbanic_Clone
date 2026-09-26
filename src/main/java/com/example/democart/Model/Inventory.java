package com.example.democart.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class Inventory {
    @Id
    private Long Inventoryid;
    private String name;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    private List<Product> prod = new ArrayList<Product>();

}
