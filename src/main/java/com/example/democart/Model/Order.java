package com.example.democart.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
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
  @TableGenerator(name = "order_id_generator", table = "order_id_sequence", pkColumnName = "sequence_name", valueColumnName = "next_id", pkColumnValue = "order", allocationSize = 1, initialValue = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "order_id_generator")
  private Long orderid;
  private String paymentMethod;
  private String transactionId;
  @Enumerated(EnumType.STRING)
  private OrderStatus orderstatus;
  @Column(nullable = false)
  private Long userid;
  @Column(nullable = false)
  private BigDecimal total;
  @Column(nullable = false)
  private String currency;
  @Column(nullable = false)
  private Instant createdAt;
  @OneToMany(mappedBy = "order", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @Builder.Default
  private List<OrderItem> items = new ArrayList<>();

}
