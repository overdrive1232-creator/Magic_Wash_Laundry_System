package org.magicwash.model;

public record InventoryItem(
        int itemId,
        String itemName,
        String category,
        int quantityInStock,
        String unitOfMeasure,
        int reorderLevel
) {}