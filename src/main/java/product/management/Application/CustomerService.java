package product.management.Application;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Domain.DTO.Customer.CustomerDTO;
import product.management.Domain.DTO.Customer.CustomerRequest;
import product.management.Infrastructure.Mappers.CustomerMapper;
import product.management.Infrastructure.Repositories.CustomerRepository;

import java.util.List;
import java.util.UUID;

@Singleton
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Inject
    public CustomerService(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CustomerDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public CustomerDTO findById(UUID id) {
        CustomerDTO customer = mapper.toDto(repository.findById(id));
        if (customer == null) {
            throw new ElementNotFoundException("Customer with id: " + id + ": doesn't exist.");
        }
        return customer;
    }

    public CustomerDTO findByEmail(String email) {
        CustomerDTO customer = mapper.toDto(repository.findByEmail(email));
        if (customer == null) {
            throw new ElementNotFoundException("Customer with email: " + email + ": doesn't exist.");
        }
        return customer;
    }

    public void delete(UUID id) {
        findById(id);
        repository.delete(id);
    }

    public CustomerDTO save(CustomerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("The request must not be empty");
        }
        return mapper.toDto(repository.save(request));
    }

    public CustomerDTO save(UUID id, CustomerRequest request) {
        findById(id);
        return mapper.toDto(repository.save(id, request));
    }
}
