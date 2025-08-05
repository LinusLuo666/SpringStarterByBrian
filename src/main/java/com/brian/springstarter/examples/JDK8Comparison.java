package com.brian.springstarter.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JDK8Comparison {

    // ======== JDK 8 STYLE EXAMPLES ========

    // 1. Verbose data class vs JDK 21 Record
    public static class UserJDK8 {
        private final Long id;
        private final String name;
        private final String email;

        public UserJDK8(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserJDK8 userJDK8 = (UserJDK8) o;
            return java.util.Objects.equals(id, userJDK8.id) &&
                   java.util.Objects.equals(name, userJDK8.name) &&
                   java.util.Objects.equals(email, userJDK8.email);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id, name, email);
        }

        @Override
        public String toString() {
            return "UserJDK8{" +
                   "id=" + id +
                   ", name='" + name + '\'' +
                   ", email='" + email + '\'' +
                   '}';
        }
    }

    // 2. Verbose instanceof vs JDK 21 pattern matching
    public static String processObjectJDK8(Object obj) {
        if (obj instanceof UserJDK8) {
            UserJDK8 user = (UserJDK8) obj; // Manual casting required
            if (user.getId() > 1000) {
                return "Premium user: " + user.getName();
            }
            return "Regular user: " + user.getName();
        } else if (obj instanceof String) {
            String str = (String) obj;
            return "String value: " + str;
        }
        return "Unknown type";
    }

    // 3. Complex switch vs JDK 21 pattern matching
    public static String getUserLevelJDK8(UserJDK8 user) {
        switch (user.getName()) {
            case "Alice":
                return "Admin";
            case "Bob":
                return "Moderator";
            default:
                return "User";
        }
    }

    // 4. Verbose try-with-resources vs JDK 21 improvements
    public static String fetchDataJDK8(String url) {
        HttpURLConnection connection = null;
        try {
            URL apiUrl = new URL(url);
            connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // 5. Traditional threads vs JDK 21 virtual threads
    public static void processTasksJDK8(int taskCount) {
        List<Thread> threads = new ArrayList<>();
        
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(100);
                    System.out.println("Task " + taskId + " completed by " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // 6. Complex string concatenation vs JDK 21 templates
    public static String createUserNotificationJDK8(UserJDK8 user, String orderDetails) {
        return "User " + user.getName() + " has placed order " + orderDetails + " with email " + user.getEmail();
    }

    // 7. Verbose collection handling vs JDK 21 sequenced collections
    public static void processListJDK8() {
        List<String> items = Arrays.asList("Apple", "Banana", "Cherry");
        
        // Get first/last elements - verbose
        String first = items.get(0);
        String last = items.get(items.size() - 1);
        
        // Reverse list - requires extra code
        List<String> reversed = new ArrayList<>(items);
        java.util.Collections.reverse(reversed);
        
        System.out.println("First: " + first + ", Last: " + last);
        System.out.println("Reversed: " + reversed);
    }

    // 8. Complex async chaining vs JDK 21 improvements
    public static CompletableFuture<String> processUserDataJDK8(UserJDK8 user) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate async processing
            try {
                Thread.sleep(100);
                return "Processed: " + user.getName();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).thenApply(result -> {
            return result.toUpperCase();
        }).thenCompose(upperResult -> {
            return CompletableFuture.supplyAsync(() -> {
                return upperResult + " - ENHANCED";
            });
        });
    }

    // ======== COMPARISON DEMONSTRATIONS ========

    public static void demonstrateAllComparisons() {
        System.out.println("=== JDK 8 vs JDK 21 Comparison ===\n");

        // 1. Data class comparison
        UserJDK8 user8 = new UserJDK8(1L, "Alice", "alice@example.com");
        JDK21Features.User user21 = new JDK21Features.User(1L, "Alice", "alice@example.com", java.time.LocalDateTime.now());
        
        System.out.println("JDK 8 User: " + user8);
        System.out.println("JDK 21 User: " + user21);
        System.out.println();

        // 2. String processing comparison
        System.out.println("JDK 8 String: " + createUserNotificationJDK8(user8, "#123"));
        System.out.println("JDK 21 String: " + 
            JDK21Features.createUserNotification(
                new JDK21Features.User(1L, "Alice", "alice@example.com", java.time.LocalDateTime.now()),
                new JDK21Features.Order(123L, null, null, 100.0)
            ));
        System.out.println();

        // 3. List processing
        System.out.println("JDK 8 List Processing:");
        processListJDK8();
        
        System.out.println("\nJDK 21 List Processing:");
        JDK21Features.demonstrateSequencedCollections();
        
        // 4. Thread performance comparison (commented to avoid execution)
        System.out.println("\n=== Performance Comparison ===");
        System.out.println("JDK 8: 1000 threads would take ~100MB memory + OS overhead");
        System.out.println("JDK 21: 1000 virtual threads take ~1MB memory total");
    }

    public static void main(String[] args) {
        demonstrateAllComparisons();
    }
}