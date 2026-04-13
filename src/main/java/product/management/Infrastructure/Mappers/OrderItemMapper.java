package product.management.Infrastructure.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import product.management.Domain.DTO.OrderItem.OrderItemDTO;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;
import product.management.Domain.Models.OrderItem;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemMapper {
    OrderItemDTO toDto(OrderItem entity);
    void toEntity(OrderItemRequest request, @MappingTarget OrderItem entity);
}