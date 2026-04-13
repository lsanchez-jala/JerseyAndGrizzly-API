package product.management.Infrastructure.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderRequest;
import product.management.Domain.Models.Order;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {
    OrderDTO toDto(Order entity);
    void toEntity(OrderRequest request, @MappingTarget Order entity);
}