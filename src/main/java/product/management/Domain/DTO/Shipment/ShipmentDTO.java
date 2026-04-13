package product.management.Domain.DTO.Shipment;

import java.time.Instant;
import java.util.UUID;

public record ShipmentDTO(
        UUID id,
        UUID orderId,
        String trackingCode,
        String carrier,
        String status,
        String createdAt,
        String updatedAt
) {}