package product.management.Domain.DTO.Shipment;

public record ShipmentRequest(
        String carrier,
        String status
) {}