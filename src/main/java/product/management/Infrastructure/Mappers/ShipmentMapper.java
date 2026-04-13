package product.management.Infrastructure.Mappers;

import jakarta.inject.Singleton;
import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;
import product.management.Domain.Enums.ShipmentStatus;
import product.management.Domain.Models.Shipment;

@Singleton
public class ShipmentMapper {

    public ShipmentDTO toDto(Shipment entity) {
        return new ShipmentDTO(
                entity.getId(),
                entity.getTrackingCode(),
                entity.getCarrier(),
                entity.getStatus().name(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt() != null
                        ? entity.getUpdatedAt().toString()
                        : null
        );
    }

    public void toEntity(ShipmentRequest request, Shipment entity) {
        if (request.trackingCode() != null)  entity.setTrackingCode(request.trackingCode());
        if (request.carrier() != null)       entity.setCarrier(request.carrier());
        if (request.status() != null)        entity.setStatus(ShipmentStatus.valueOf(request.status()));
    }
}
