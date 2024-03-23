package com.shubham.orderservice.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.shubham.orderservice.dto.InventoryResponse;
import com.shubham.orderservice.dto.OrderPlacedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shubham.orderservice.dto.OrderLineItemDto;
import com.shubham.orderservice.dto.OrderRequest;
import com.shubham.orderservice.entity.Order;
import com.shubham.orderservice.entity.OrderLineItem;
import com.shubham.orderservice.repository.OrderRepository;
import com.shubham.orderservice.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;
    private static final String INVENTORY_SERVICE_URI = "http://inventory-service/api/inventory";

    private final NewTopic topic;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    /**
     * Places an order based on the order request
     *
     * @param orderRequest the order request containing order line items
     */
    @Override
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
// Map order line item DTOs to entities and set the order for each line item
        Set<OrderLineItem> orderLineItems = orderRequest.getOrderLineItemDtoList().stream()
                .map(this::mapFromDtoToEntity).peek(item -> item.setOrder(order)).collect(Collectors.toSet());
        order.setOrderLineItemList(orderLineItems);

// Get the list of SKU codes from the order line items
        List<String> skuCodes = orderLineItems.stream().map(OrderLineItem::getSkuCode).toList();

        // Call the inventory service to check the stock availability for the SKU codes
        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                .uri(INVENTORY_SERVICE_URI, uriBuilder ->
                        uriBuilder.queryParam("skuCode", skuCodes)
                                .build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        assert inventoryResponseArray != null;
        // Check if all products are in stock
        boolean allProductInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);
        // Save the order if all products are in stock, otherwise throw an exception
        if (allProductInStock) {
            orderRepository.save(order);
            log.info("order {} is Placed ", order.getId());

            //through topic send msg to notification service
            Message<OrderPlacedEvent> message = MessageBuilder
                    .withPayload(new OrderPlacedEvent(order.getOrderNumber()))
                    .setHeader(KafkaHeaders.TOPIC, topic.name())
                    .build();
            kafkaTemplate.send(message);

            return "Order Placed Successfully...";
        } else {
            throw new IllegalArgumentException("Product is not in Stock, please try again later..");
        }


    }

    /**
     * Maps from the order line item DTO to the entity.
     *
     * @param orderLineItemDto the order line item DTO to map from
     * @return the mapped order line item entity
     */

    public OrderLineItem mapFromDtoToEntity(OrderLineItemDto orderLineItemDto) {
        return OrderLineItem.builder().skuCode(orderLineItemDto.getSkuCode()).price(orderLineItemDto.getPrice())
                .quantity(orderLineItemDto.getQuantity()).build();

    }


}
