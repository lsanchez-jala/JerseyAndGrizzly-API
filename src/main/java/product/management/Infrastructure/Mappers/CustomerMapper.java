package product.management.Infrastructure.Mappers;

import jakarta.inject.Singleton;
import product.management.Domain.DTO.Customer.CustomerDTO;
import product.management.Domain.DTO.Customer.CustomerRequest;
import product.management.Domain.Models.Customer;

@Singleton
public class CustomerMapper {

    public CustomerDTO toDto(Customer entity) {
        return new CustomerDTO(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt() != null
                        ? entity.getUpdatedAt().toString()
                        : null
        );
    }

    public void toEntity(CustomerRequest request, Customer entity) {
        if (request.firstName() != null) entity.setFirstName(request.firstName());
        if (request.lastName() != null)  entity.setLastName(request.lastName());
        if (request.email() != null)     entity.setEmail(request.email());
    }
}
