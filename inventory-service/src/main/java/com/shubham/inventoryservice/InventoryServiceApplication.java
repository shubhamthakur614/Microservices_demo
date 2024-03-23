package com.shubham.inventoryservice;

import com.shubham.inventoryservice.entity.Inventory;
import com.shubham.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
        return args -> {
            Inventory i = new Inventory();
            i.setSkuCode("iphone_13");
            i.setQuantity(100);

            Inventory i1 = new Inventory();
            i1.setSkuCode("iphone_13_red");
            i1.setQuantity(0);
            inventoryRepository.save(i);
            inventoryRepository.save(i1);
        };
    }
}
