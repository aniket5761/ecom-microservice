package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/add")
    public void addStock(@Valid @RequestBody AddStockRequest request) {
        inventoryService.addStock(request);
    }

    @PostMapping("/reserve")
    public StockResponse reserve(@Valid @RequestBody ReserveStockRequest request) {
        return inventoryService.reserveStock(request);
    }

    @PostMapping("/confirm")
    public void confirm(@Valid @RequestBody ReserveStockRequest request) {
        inventoryService.confirmStock(request);
    }

    @PostMapping("/release")
    public void release(@Valid @RequestBody ReserveStockRequest request) {
        inventoryService.releaseStock(request);
    }
}
