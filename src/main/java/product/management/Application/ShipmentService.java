package product.management.Application;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;
import product.management.Infrastructure.Mappers.ShipmentMapper;
import product.management.Infrastructure.Repositories.ShipmentRepository;

import java.util.List;
import java.util.UUID;

@Singleton
public class ShipmentService {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;

    @Inject
    public ShipmentService(ShipmentRepository repository, ShipmentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ShipmentDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public ShipmentDTO findById(UUID id) {
        ShipmentDTO shipment = mapper.toDto(repository.findById(id));
        if (shipment == null) {
            throw new ElementNotFoundException("Shipment with id: " + id + ": doesn't exist.");
        }
        return shipment;
    }

    public ShipmentDTO findByOrderId(UUID orderId) {
        ShipmentDTO shipment = mapper.toDto(repository.findByOrderId(orderId));
        if (shipment == null) {
            throw new ElementNotFoundException("Shipment for order id: " + orderId + ": doesn't exist.");
        }
        return shipment;
    }

    public ShipmentDTO findByTrackingCode(String trackingCode) {
        ShipmentDTO shipment = mapper.toDto(repository.findByTrackingCode(trackingCode));
        if (shipment == null) {
            throw new ElementNotFoundException("Shipment with tracking code: " + trackingCode + ": doesn't exist.");
        }
        return shipment;
    }

    public void delete(UUID id) {
        findById(id);
        repository.delete(id);
    }

    public ShipmentDTO save(ShipmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("The request must not be empty");
        }
        return mapper.toDto(repository.save(request));
    }

    public ShipmentDTO save(UUID id, ShipmentRequest request) {
        findById(id);
        return mapper.toDto(repository.save(id, request));
    }
}