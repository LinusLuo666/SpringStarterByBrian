package com.brian.springstarter.examples;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v4")
public class SpringBoot4Features {

    // Spring Boot 4 - Full reactive stack with WebFlux
    
    record Product(Long id, String name, double price, String category) {}
    
    record Order(Long id, Long userId, List<OrderItem> items, double total) {}
    
    record OrderItem(Long productId, int quantity, double price) {}

    // 1. Reactive REST endpoints - non-blocking I/O
    @GetMapping(value = "/products/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> streamProducts() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> new Product(i, "Product-" + i, 100.0 + i, "Electronics"));
    }

    // 2. Reactive CRUD with non-blocking database calls
    @GetMapping("/products/{id}")
    public Mono<Product> getProduct(@PathVariable Long id) {
        // Simulating reactive database call
        return Mono.just(new Product(id, "Smartphone", 699.99, "Electronics"))
                .delayElement(Duration.ofMillis(100)); // Simulate latency
    }

    // 3. Reactive aggregation - combining multiple async calls
    @GetMapping("/users/{userId}/orders")
    public Flux<Order> getUserOrders(@PathVariable Long userId) {
        // Simulating reactive calls to multiple services
        return Flux.range(1, 5)
                .map(i -> new Order(
                        (long) i,
                        userId,
                        List.of(new OrderItem((long) (i * 10), i, 50.0 * i)),
                        100.0 * i
                ))
                .delayElements(Duration.ofMillis(50));
    }

    // 4. HTTP Interface clients - declarative HTTP clients (Spring 6+)
    @HttpExchange(url = "/external-api")
    public interface ProductClient {
        @GetExchange("/products/{id}")
        Mono<Product> getProduct(@PathVariable Long id);
        
        @GetExchange("/products")
        Flux<Product> getProducts();
    }

    // 5. Functional endpoints with Router Functions
    @Service
    public static class ProductService {
        
        public Flux<Product> searchProducts(String query, double minPrice, double maxPrice) {
            return Flux.just(
                    new Product(1L, "iPhone", 999.0, "Electronics"),
                    new Product(2L, "Samsung Galaxy", 899.0, "Electronics"),
                    new Product(3L, "Google Pixel", 799.0, "Electronics")
            )
            .filter(p -> p.name().toLowerCase().contains(query.toLowerCase()))
            .filter(p -> p.price() >= minPrice && p.price() <= maxPrice);
        }

        public Mono<Map<String, Object>> getProductAnalytics() {
            return Flux.just(
                    new Product(1L, "Laptop", 1200.0, "Electronics"),
                    new Product(2L, "Mouse", 25.0, "Electronics"),
                    new Product(3L, "Keyboard", 75.0, "Electronics")
            )
            .collectList()
            .map(products -> Map.of(
                    "totalProducts", products.size(),
                    "totalValue", products.stream().mapToDouble(Product::price).sum(),
                    "categories", products.stream().map(Product::category).distinct().toList()
            ));
        }
    }

    // 6. Reactive error handling
    @GetMapping("/products/{id}/details")
    public Mono<Product> getProductDetails(@PathVariable Long id) {
        return Mono.just(id)
                .flatMap(productId -> {
                    if (productId <= 0) {
                        return Mono.error(new IllegalArgumentException("Invalid product ID"));
                    }
                    return Mono.just(new Product(productId, "Product " + productId, 100.0, "Category"));
                })
                .onErrorResume(IllegalArgumentException.class, 
                    ex -> Mono.just(new Product(0L, "Default Product", 0.0, "None")));
    }

    // 7. Server-Sent Events with reactive streams
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getNotifications() {
        return Flux.interval(Duration.ofSeconds(2))
                .map(i -> "Notification " + i + " at " + java.time.LocalTime.now());
    }

    // 8. Reactive file processing (simulation)
    @GetMapping("/process-csv")
    public Flux<String> processCsvData() {
        // Simulating processing large CSV file reactively
        return Flux.just("product1,100.0,10", "product2,200.0,20", "product3,300.0,30")
                .map(line -> {
                    String[] parts = line.split(",");
                    return "Processed: " + parts[0] + " with revenue " + (Double.parseDouble(parts[1]) * Integer.parseInt(parts[2]));
                });
    }
}