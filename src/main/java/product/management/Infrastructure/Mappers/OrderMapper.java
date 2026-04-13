package product.management.Infrastructure.Mappers;

import jakarta.inject.Singleton;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderRequest;
import product.management.Domain.Models.Order;

@Singleton
public class OrderMapper {

    public OrderDTO toDto(Order entity) {
        return new OrderDTO(
                entity.getId(),
                entity.getCustomerId(),
                entity.getShipmentId(),
                entity.getStatus().name(),
                entity.getTotalAmount(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt() != null
                        ? entity.getUpdatedAt().toString()
                        : null
        );
    }

    public void toEntity(OrderRequest request, Order entity) {
        if (request.customerId() != null)   entity.setCustomerId(request.customerId());
        if (request.shipmentId() != null)   entity.setShipmentId(request.shipmentId());
        if (request.status() != null)       entity.setStatus(request.status());
        if (request.totalAmount() != null)  entity.setTotalAmount(request.totalAmount());
    }
}