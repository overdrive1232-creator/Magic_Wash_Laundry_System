package org.magicwash.model;

public record User(
        int userId,
        String username,
        String passwordHash,
        String fullName,
        String userRole,
        String status
) {}
