package com.shubham.orderservice.service;

import com.shubham.orderservice.dto.OrderRequest;

public interface OrderService {

	String  placeOrder(OrderRequest orderRequest);

}
