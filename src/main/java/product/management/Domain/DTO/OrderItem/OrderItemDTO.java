package product.management.Domain.DTO.OrderItem;

import java.util.UUID;

public record OrderItemDTO(
        UUID id,
        UUID orderId,
        UUID productId,
        int quantity,
        Float unitPrice
) {}