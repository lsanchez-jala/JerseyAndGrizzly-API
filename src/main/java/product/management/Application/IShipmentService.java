package product.management.Application;

import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;

import java.util.List;
import java.util.UUID;

public interface IShipmentService {
    List<ShipmentDTO> findAll();
    ShipmentDTO findById(UUID id);
    ShipmentDTO findByTrackingCode(String trackingCode);
    void delete(UUID id);
    ShipmentDTO save(ShipmentRequest request);
    ShipmentDTO save(UUID id, ShipmentRequest request);
    ShipmentDTO changeStatus(UUID shipmentId, ShipmentRequest request);
}
