package product.management.Infrastructure.Repositories;

import product.management.Domain.DTO.OrderItem.OrderItemRequest;
import product.management.Domain.Models.OrderItem;

import java.util.List;
import java.util.UUID;

public interface IOrderItemRepository {
    List<OrderItem> findAll();
    OrderItem findById(UUID id);
    List<OrderItem> findByOrderId(UUID orderId);
    List<OrderItem> findByProductId(UUID productId);
    OrderItem save(OrderItemRequest request);
    OrderItem save(UUID id, OrderItemRequest request);
    void delete(UUID id);
    void deleteByOrderId(UUID orderId);
}
