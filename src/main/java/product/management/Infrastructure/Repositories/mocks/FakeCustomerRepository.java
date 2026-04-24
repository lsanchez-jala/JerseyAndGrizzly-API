package product.management.Infrastructure.Repositories.mocks;

import product.management.Domain.DTO.Customer.CustomerRequest;
import product.management.Domain.Models.Customer;
import product.management.Infrastructure.Repositories.ICustomerRepository;

import java.time.Instant;
import java.util.*;

public class FakeCustomerRepository implements ICustomerRepository {

    private final Map<UUID, Customer> store = new HashMap<>();

    public void add(Customer customer) {
        store.put(customer.getId(), customer);
    }

    public Map<UUID, Customer> getStore() {
        return Collections.unmodifiableMap(store);
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Customer findById(UUID id) {
        return store.get(id);
    }

    @Override
    public Customer findByEmail(String email) {
        return store.values().stream()
                .filter(c -> email.equals(c.getEmail()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Customer save(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        store.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public Customer save(UUID id, CustomerRequest request) {
        Customer existing = store.get(id);
        if (existing == null) {
            throw new RuntimeException("No customer found with id: " + id);
        }
        if (request.firstName() != null) existing.setFirstName(request.firstName());
        if (request.lastName() != null)  existing.setLastName(request.lastName());
        if (request.email() != null)     existing.setEmail(request.email());
        existing.setUpdatedAt(Instant.now());
        store.put(id, existing);
        return existing;
    }

    @Override
    public void delete(UUID id) {
        if (!store.containsKey(id)) {
            throw new RuntimeException("No customer found with id: " + id);
        }
        store.remove(id);
    }
}