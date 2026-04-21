package product.management.Application;

import product.management.Domain.DTO.OrderItem.OrderItemDTO;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;

import java.util.List;
import java.util.UUID;

public interface IOrderItemService {
    List<OrderItemDTO> findAll();
    OrderItemDTO findById(UUID id);
    List<OrderItemDTO> findByOrderId(UUID orderId);
    List<OrderItemDTO> findByProductId(UUID productId);
    void delete(UUID id);
    void deleteByOrderId(UUID orderId);
    OrderItemDTO save(OrderItemRequest request);
    OrderItemDTO save(UUID id, OrderItemRequest request);
}
