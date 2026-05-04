package Mocks.Repositories;

import product.management.Domain.DTO.OrderItem.OrderItemRequest;
import product.management.Domain.Models.OrderItem;
import product.management.Infrastructure.Repositories.IOrderItemRepository;

import java.util.*;

public class FakeOrderItemRepository implements IOrderItemRepository {

    private final Map<UUID, OrderItem> store = new HashMap<>();

    public void add(OrderItem orderItem) {
        store.put(orderItem.getId(), orderItem);
    }

    public Map<UUID, OrderItem> getStore() {
        return Collections.unmodifiableMap(store);
    }

    @Override
    public List<OrderItem> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public OrderItem findById(UUID id) {
        return store.get(id);
    }

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return store.values().stream()
                .filter(i -> orderId.equals(i.getOrderId()))
                .toList();
    }

    @Override
    public List<OrderItem> findByProductId(UUID productId) {
        return store.values().stream()
                .filter(i -> productId.equals(i.getProductId()))
                .toList();
    }

    @Override
    public OrderItem save(OrderItemRequest request) {
        OrderItem item = new OrderItem();
        item.setId(UUID.randomUUID());
        item.setOrderId(request.orderId());
        item.setProductId(request.productId());
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        store.put(item.getId(), item);
        return item;
    }

    @Override
    public OrderItem save(UUID id, OrderItemRequest request) {
        OrderItem existing = store.get(id);
        if (existing == null) {
            throw new RuntimeException("No order item found with id: " + id);
        }
        if (request.orderId() != null)   existing.setOrderId(request.orderId());
        if (request.productId() != null) existing.setProductId(request.productId());
        existing.setQuantity(request.quantity());
        existing.setUnitPrice(request.unitPrice());
        store.put(id, existing);
        return existing;
    }

    @Override
    public void delete(UUID id) {
        if (!store.containsKey(id)) {
            throw new RuntimeException("No order item found with id: " + id);
        }
        store.remove(id);
    }

    @Override
    public void deleteByOrderId(UUID orderId) {
        store.entrySet().removeIf(entry -> orderId.equals(entry.getValue().getOrderId()));
    }
}