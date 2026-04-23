package product.management.Infrastructure.Repositories.Fakes;

import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Order;
import product.management.Infrastructure.Repositories.IOrderRepository;

import java.time.Instant;
import java.util.*;

public class FakeOrderRepository implements IOrderRepository {

    private final Map<UUID, Order> store = new HashMap<>();

    // Helper to pre-populate in tests
    public void add(Order order) {
        store.put(order.getId(), order);
    }

    public Map<UUID, Order> getStore() {
        return Collections.unmodifiableMap(store);
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Order findById(UUID id) {
        return store.get(id); // returns null if not found, matching real repo behavior
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return store.values().stream()
                .filter(o -> customerId.equals(o.getCustomerId()))
                .toList();
    }

    @Override
    public List<Order> findByShipmentId(UUID shipmentId) {
        return store.values().stream()
                .filter(o -> shipmentId.equals(o.getShipmentId()))
                .toList();
    }

    @Override
    public Order save(Order request) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Order save(UUID id, Order request) {
        Order existing = store.get(id);
        if (existing == null) {
            throw new RuntimeException("No order found with id: " + id);
        }
        if (request.getCustomerId() != null)  existing.setCustomerId(request.getCustomerId());
        if (request.getShipmentId() != null)  existing.setShipmentId(request.getShipmentId());
        existing.setUpdatedAt(Instant.now());
        store.put(id, existing);
        return existing;
    }

    @Override
    public void delete(UUID id) {
        if (!store.containsKey(id)) {
            throw new RuntimeException("No order found with id: " + id);
        }
        store.remove(id);
    }

    @Override
    public Order updateStatus(UUID orderId, OrderStatus newStatus) {
        Order existing = store.get(orderId);
        if (existing == null) {
            throw new RuntimeException("No order found with id: " + orderId);
        }
        existing.setStatus(newStatus);
        existing.setUpdatedAt(Instant.now());
        store.put(orderId, existing);
        return existing;
    }
}