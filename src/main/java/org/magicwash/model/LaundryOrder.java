package org.magicwash.model;

public record LaundryOrder(
        int orderId,
        int customerId,
        String serviceType,
        double weightKg,
        double totalAmount,
        String orderStatus,
        String orderDate,
        int processedByUserId
) {}