package com.shubham.orderservice.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.shubham.orderservice.dto.OrderRequest;
import com.shubham.orderservice.service.OrderService;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/order")
@RequiredArgsConstructor
public class

OrderController {

	private final OrderService orderService;

	@PostMapping
    @ResponseStatus(HttpStatus.OK)
    @CircuitBreaker(name="incentory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name="inventory")
    @Retry(name="inventory" )
	public CompletableFuture<String> placeOrd(@RequestBody OrderRequest orderRequest) {
	return CompletableFuture.supplyAsync(()->orderService.placeOrder(orderRequest));


	}
    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest,RuntimeException e){
        return CompletableFuture.supplyAsync(()->"Oops! Something went Wrong, Please order after Some time!");
    }

}
