package org.magicwash.model;

public record Customer(
        int customerId,
        String firstName,
        String lastName,
        String contactNumber,
        String createdAt
) {}
