package product.management.Infrastructure.Repositories;

import product.management.Domain.DTO.Customer.CustomerRequest;
import product.management.Domain.Models.Customer;

import java.util.List;
import java.util.UUID;

public interface ICustomerRepository {
    List<Customer> findAll();
    Customer findById(UUID id);
    Customer findByEmail(String email);
    Customer save(CustomerRequest request);
    Customer save(UUID id, CustomerRequest request);
    void delete(UUID id) ;
}
