package product.management.Domain.DTO.OrderItem;

import java.util.UUID;

public record OrderItemRequest(
        UUID orderId,
        UUID productId,
        int quantity,
        Float unitPrice
) {}