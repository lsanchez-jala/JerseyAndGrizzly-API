package product.management.Domain.DTO.Order;

import product.management.Domain.Enums.OrderStatus;

import java.util.UUID;

public record OrderRequest(
        UUID customerId,
        UUID shipmentId,
        OrderStatus status,
        Float totalAmount
) {}
