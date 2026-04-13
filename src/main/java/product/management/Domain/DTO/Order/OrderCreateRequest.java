package product.management.Domain.DTO.Order;

import java.util.UUID;

public record OrderCreateRequest(
        UUID customerId,
        UUID shipmentId
) {}
