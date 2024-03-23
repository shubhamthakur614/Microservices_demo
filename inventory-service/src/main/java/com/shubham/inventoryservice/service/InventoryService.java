package com.shubham.inventoryservice.service;

import com.shubham.inventoryservice.dto.InventoryResponse;

import java.util.List;

public interface InventoryService {

    List<InventoryResponse> isInStock(List<String> skuCode);

}
