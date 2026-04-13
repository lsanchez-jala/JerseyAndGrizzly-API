package product.management.Domain.DTO.Shipment;

import java.util.UUID;

public record ShipmentDTO(
        UUID id,
        String trackingCode,
        String carrier,
        String status,
        String createdAt,
        String updatedAt
) {}