package product.management.Infrastructure.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;
import product.management.Domain.Models.Shipment;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShipmentMapper {
    ShipmentDTO toDto(Shipment entity);
    void toEntity(ShipmentRequest request, @MappingTarget Shipment entity);
}
