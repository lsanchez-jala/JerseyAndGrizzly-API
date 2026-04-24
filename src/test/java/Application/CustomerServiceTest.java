package Application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.management.Application.exception.BadRequestException;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Application.impl.CustomerService;
import product.management.Domain.DTO.Customer.CustomerDTO;
import product.management.Domain.DTO.Customer.CustomerRequest;
import product.management.Domain.Models.Customer;
import product.management.Infrastructure.Mappers.CustomerMapper;
import product.management.Infrastructure.Repositories.mocks.FakeCustomerRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerServiceTest {

    private FakeCustomerRepository repository;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        repository = new FakeCustomerRepository();
        service = new CustomerService(repository, new CustomerMapper());
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void givenNoCustomers_whenFindAll_thenReturnsEmptyList() {
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void givenCustomers_whenFindAll_thenReturnsAllMapped() {
        repository.add(buildCustomer("John", "Doe", "john@example.com"));
        repository.add(buildCustomer("Jane", "Doe", "jane@example.com"));

        List<CustomerDTO> result = service.findAll();

        assertEquals(2, result.size());
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenFindById_thenReturnsDTO() {
        Customer customer = buildCustomer("John", "Doe", "john@example.com");
        repository.add(customer);

        CustomerDTO result = service.findById(customer.getId());

        assertEquals(customer.getId(), result.id());
        assertEquals("John", result.firstName());
    }

    @Test
    void givenNonExistingId_whenFindById_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.findById(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // findByEmail
    // -------------------------------------------------------------------------

    @Test
    void givenExistingEmail_whenFindByEmail_thenReturnsDTO() {
        repository.add(buildCustomer("John", "Doe", "john@example.com"));

        CustomerDTO result = service.findByEmail("john@example.com");

        assertEquals("john@example.com", result.email());
    }

    @Test
    void givenNonExistingEmail_whenFindByEmail_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.findByEmail("unknown@example.com"));
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenCustomerIsRemoved() {
        Customer customer = buildCustomer("John", "Doe", "john@example.com");
        repository.add(customer);

        service.delete(customer.getId());

        assertNull(repository.findById(customer.getId()));
    }

    @Test
    void givenNonExistingId_whenDelete_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.delete(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // save (create)
    // -------------------------------------------------------------------------

    @Test
    void givenValidRequest_whenSave_thenCustomerIsPersisted() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com");

        CustomerDTO result = service.save(request);

        assertNotNull(result.id());
        assertNotNull(repository.findByEmail("john@example.com"));
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
    }

    @Test
    void givenNullRequest_whenSave_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.save( null));
    }

    @Test
    void givenInvalidEmail_whenSave_thenThrowsBadRequestException() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "invalid-email");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenEmailWithoutDomain_whenSave_thenThrowsBadRequestException() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenEmailWithoutAtSign_whenSave_thenThrowsBadRequestException() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "johnexample.com");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenDuplicateEmail_whenSave_thenThrowsBadRequestException() {
        repository.add(buildCustomer("John", "Doe", "john@example.com"));
        CustomerRequest request = new CustomerRequest("Jane", "Doe", "john@example.com");

        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    // -------------------------------------------------------------------------
    // save (update)
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenSaveWithId_thenCustomerIsUpdated() {
        Customer customer = buildCustomer("John", "Doe", "john@example.com");
        repository.add(customer);
        CustomerRequest request = new CustomerRequest("Johnny", "Doe", "johnny@example.com");

        CustomerDTO result = service.save(customer.getId(), request);

        assertEquals("Johnny", result.firstName());
        assertEquals("johnny@example.com", result.email());
    }

    @Test
    void givenNonExistingId_whenSaveWithId_thenThrowsElementNotFoundException() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com");

        assertThrows(ElementNotFoundException.class,
                () -> service.save(UUID.randomUUID(), request));
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Customer buildCustomer(String firstName, String lastName, String email) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        return customer;
    }
}
