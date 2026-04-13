package product.management.Domain.DTO.Shipment;

public record ShipmentRequest(
        String trackingCode,
        String carrier,
        String status
) {}