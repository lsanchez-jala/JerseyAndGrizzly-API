package product.management.Application;

import product.management.Domain.DTO.Order.OrderCreateRequest;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderStatusRequest;

import java.util.List;
import java.util.UUID;

public interface IOrderService {
    List<OrderDTO> findAll();
    OrderDTO findById(UUID id);
    List<OrderDTO> findByCustomerId(UUID customerId);
    List<OrderDTO> findByShipmentId(UUID shipmentId);
    void delete(UUID id);
    OrderDTO save(OrderCreateRequest request);
    OrderDTO save(UUID id, OrderCreateRequest request);
    OrderDTO changeStatus(UUID orderId, OrderStatusRequest request);
}
