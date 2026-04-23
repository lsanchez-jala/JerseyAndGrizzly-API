package product.management.Infrastructure.Repositories.Fakes;

import product.management.Domain.Enums.ShipmentStatus;
import product.management.Domain.Models.Shipment;
import product.management.Infrastructure.Repositories.IShipmentRepository;

import java.time.Instant;
import java.util.*;

public class FakeShipmentRepository implements IShipmentRepository {

    private final Map<UUID, Shipment> store = new HashMap<>();

    public void add(Shipment shipment) {
        store.put(shipment.getId(), shipment);
    }

    public Map<UUID, Shipment> getStore() {
        return Collections.unmodifiableMap(store);
    }

    @Override
    public List<Shipment> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Shipment findById(UUID id) {
        return store.get(id);
    }

    @Override
    public Shipment findByTrackingCode(String trackingCode) {
        return store.values().stream()
                .filter(s -> trackingCode.equals(s.getTrackingCode()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Shipment save(Shipment request) {
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setTrackingCode(request.getTrackingCode());
        shipment.setCarrier(request.getCarrier());
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setCreatedAt(Instant.now());
        shipment.setUpdatedAt(Instant.now());
        store.put(shipment.getId(), shipment);
        return shipment;
    }

    @Override
    public Shipment save(UUID id, Shipment request) {
        Shipment existing = store.get(id);
        if (existing == null) {
            throw new RuntimeException("No shipment found with id: " + id);
        }
        if (request.getCarrier() != null) existing.setCarrier(request.getCarrier());
        existing.setUpdatedAt(Instant.now());
        store.put(id, existing);
        return existing;
    }

    @Override
    public Shipment updateStatus(UUID shipmentId, ShipmentStatus newStatus) {
        Shipment existing = store.get(shipmentId);
        if (existing == null) {
            throw new RuntimeException("No shipment found with id: " + shipmentId);
        }
        existing.setStatus(newStatus);
        existing.setUpdatedAt(Instant.now());
        store.put(shipmentId, existing);
        return existing;
    }

    @Override
    public void delete(UUID id) {
        if (!store.containsKey(id)) {
            throw new RuntimeException("No shipment found with id: " + id);
        }
        store.remove(id);
    }
}