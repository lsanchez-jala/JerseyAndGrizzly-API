package product.management.Infrastructure.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import product.management.Domain.DTO.Customer.CustomerDTO;
import product.management.Domain.DTO.Customer.CustomerRequest;
import product.management.Domain.Models.Customer;

@Mapper( nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {
    CustomerDTO toDto(Customer entity);
    void toEntity(CustomerRequest request, @MappingTarget Customer entity);
}
