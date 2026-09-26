package com.example.democart.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "orders")
public class Order {
  @Id
  private Long orderid;
  private String Paymentmathod;
  private Long Transactionid;
  @Enumerated(EnumType.STRING)
  private OrderStatus orderstatus;
  @Column(nullable = false)
  private Long userid;
  @ManyToOne
  private Product product;

}
