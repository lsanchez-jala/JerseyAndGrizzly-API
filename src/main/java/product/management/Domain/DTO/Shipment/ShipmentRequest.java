package product.management.Domain.DTO.Shipment;

import java.util.UUID;

public record ShipmentRequest(
        UUID orderId,
        String trackingCode,
        String carrier,
        String status
) {}