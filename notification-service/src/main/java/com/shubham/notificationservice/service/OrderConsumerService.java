package com.shubham.notificationservice.service;

import com.shubham.notificationservice.dto.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderConsumerService {

    private final Logger logger= LoggerFactory.getLogger(OrderConsumerService.class);
@KafkaListener(topics="${spring.kafka.topic.name}",groupId= "${spring.kafka.consumer.group-id}")
    public void consumeEvent(OrderPlacedEvent orderPlacedEvent){
        logger.info(String.format("Order Placed Event Received Successfully for Order Number=> %s",orderPlacedEvent.getOrderNumber()));
    }

}
