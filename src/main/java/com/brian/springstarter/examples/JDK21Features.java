package com.brian.springstarter.examples;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class JDK21Features {

    // JDK 21 Record - concise immutable data classes
    public record User(Long id, String name, String email, LocalDateTime createdAt) {}
    
    public record Order(Long id, User user, List<OrderItem> items, double total) {}
    
    public record OrderItem(String product, int quantity, double price) {}

    // Pattern matching with switch - JDK 21
    public static String processOrder(Order order) {
        return switch (order) {
            case Order(var id, var user, var items, var total) when total > 1000 ->
                "High value order: " + id + " for " + user.name();
            case Order(var id, var user, var items, var total) when total > 100 ->
                "Medium value order: " + id;
            case Order(var id, var user, var items, var total) ->
                "Regular order: " + id;
        };
    }

    // Pattern matching with instanceof - much cleaner than JDK 8
    public static String getOrderDescription(Object obj) {
        if (obj instanceof Order(var id, var user, var items, var total) && total > 500) {
            return "Premium order #" + id + " for " + user.name();
        }
        return "Standard order";
    }

    // Virtual threads - massive performance improvement over traditional threads
    public static void demonstrateVirtualThreads() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> {
                executor.submit(() -> {
                    // Simulate I/O operation
                    try {
                        Thread.sleep(100);
                        return "Task " + i + " completed by " + Thread.currentThread();
                    } catch (InterruptedException e) {
                        return "Task interrupted";
                    }
                });
            });
        }
    }

    // Sequenced collections - JDK 21
    public static void demonstrateSequencedCollections() {
        var items = List.of("Apple", "Banana", "Cherry");
        
        // JDK 21 - get first/last elements directly
        var first = items.getFirst();
        var last = items.getLast();
        
        // Reversed view
        var reversed = items.reversed();
        
        System.out.println("First: " + first + ", Last: " + last);
        System.out.println("Reversed: " + reversed);
    }

    // String formatting with String.format (replaced String templates to avoid preview issues)
    public static String createUserNotification(User user, Order order) {
        return String.format("User %s has placed order #%d with total $%.2f", 
                           user.name(), order.id(), order.total());
    }

    // Main method to run examples
    public static void main(String[] args) {
        var user = new User(1L, "Alice Johnson", "alice@example.com", LocalDateTime.now());
        var items = List.of(new OrderItem("Laptop", 1, 1200.0), new OrderItem("Mouse", 2, 25.0));
        var order = new Order(1001L, user, items, 1250.0);

        System.out.println("=== JDK 21 Features Demo ===");
        System.out.println(processOrder(order));
        System.out.println(getOrderDescription(order));
        System.out.println(createUserNotification(user, order));
        
        demonstrateSequencedCollections();
        
        // Note: Virtual threads demo is commented out to avoid overwhelming output
        // demonstrateVirtualThreads();
    }
}