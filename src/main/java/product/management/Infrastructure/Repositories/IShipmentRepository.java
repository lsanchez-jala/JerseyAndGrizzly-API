package product.management.Infrastructure.Repositories;

import product.management.Domain.Enums.ShipmentStatus;
import product.management.Domain.Models.Shipment;

import java.util.List;
import java.util.UUID;

public interface IShipmentRepository {
    List<Shipment> findAll();
    Shipment findById(UUID id);
    Shipment findByTrackingCode(String trackingCode);
    Shipment save(Shipment request);
    Shipment save(UUID id, Shipment request);
    Shipment updateStatus(UUID shipmentId, ShipmentStatus newStatus);
    void delete(UUID id);
}
