package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void addStock(AddStockRequest request) {

        Inventory inventory = inventoryRepository
                .findByProductId(request.getProductId())
                .orElse(
                        Inventory.builder()
                                .productId(request.getProductId())
                                .availableQuantity(0)
                                .reservedQuantity(0)
                                .createdAt(LocalDateTime.now())
                                .build()
                );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + request.getQuantity()
        );

        inventoryRepository.save(inventory);
    }

    @Transactional
    public StockResponse reserveStock(ReserveStockRequest request) {

        Inventory inventory = inventoryRepository
                .findByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            return StockResponse.builder()
                    .success(false)
                    .message("Insufficient stock")
                    .build();
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - request.getQuantity()
        );

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + request.getQuantity()
        );

        inventoryRepository.save(inventory);

        return StockResponse.builder()
                .success(true)
                .message("Stock reserved successfully")
                .build();
    }

    @Transactional
    public void confirmStock(ReserveStockRequest request) {

        Inventory inventory = inventoryRepository
                .findByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - request.getQuantity()
        );

        inventoryRepository.save(inventory);
    }

    @Transactional
    public void releaseStock(ReserveStockRequest request) {

        Inventory inventory = inventoryRepository
                .findByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - request.getQuantity()
        );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + request.getQuantity()
        );

        inventoryRepository.save(inventory);
    }
}

