package product.management.Infrastructure.Repositories;

import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Order;

import java.util.List;
import java.util.UUID;

public interface IOrderRepository {
    List<Order> findAll();
    Order findById(UUID id);
    List<Order> findByCustomerId(UUID customerId);
    List<Order> findByShipmentId(UUID shipmentId);
    Order save(Order request);
    Order save(UUID id, Order request);
    void delete(UUID id);
    Order updateStatus(UUID orderId, OrderStatus newStatus);
}
