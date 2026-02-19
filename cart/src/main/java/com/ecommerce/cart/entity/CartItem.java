package com.ecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID cartId;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;
}
