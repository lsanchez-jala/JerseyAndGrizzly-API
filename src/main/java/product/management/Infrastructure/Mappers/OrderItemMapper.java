package product.management.Infrastructure.Mappers;

import jakarta.inject.Singleton;
import product.management.Domain.DTO.OrderItem.OrderItemDTO;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;
import product.management.Domain.Models.OrderItem;

@Singleton
public class OrderItemMapper {

    public OrderItemDTO toDto(OrderItem entity) {
        return new OrderItemDTO(
                entity.getId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );
    }

    public void toEntity(OrderItemRequest request, OrderItem entity) {
        if (request.orderId() != null)   entity.setOrderId(request.orderId());
        if (request.productId() != null) entity.setProductId(request.productId());
        if (request.unitPrice() != null) entity.setUnitPrice(request.unitPrice());
        entity.setQuantity(request.quantity()); // primitive int, cannot be null
    }
}