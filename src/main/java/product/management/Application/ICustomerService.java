package product.management.Application;

import product.management.Domain.DTO.Customer.CustomerDTO;
import product.management.Domain.DTO.Customer.CustomerRequest;

import java.util.List;
import java.util.UUID;

public interface ICustomerService {
    List<CustomerDTO> findAll();
    CustomerDTO findById(UUID id);
    CustomerDTO findByEmail(String email);
    void delete(UUID id);
    CustomerDTO save(CustomerRequest request);
    CustomerDTO save(UUID id, CustomerRequest request);
}
