package product.management.Domain.DTO.Order;

import java.time.Instant;
import java.util.UUID;

public record OrderDTO(
        UUID id,
        UUID customerId,
        UUID shipmentId,
        String status,
        Float totalAmount,
        String createdAt,
        String updatedAt
) {}