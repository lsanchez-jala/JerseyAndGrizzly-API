package API;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import product.management.API.CustomerResource;
import product.management.Application.ICustomerService;
import product.management.Application.exception.GenericExceptionMapper;
import product.management.Application.impl.CustomerService;
import product.management.Domain.DTO.Customer.CustomerRequest;
import product.management.Domain.Models.Customer;
import product.management.Infrastructure.Mappers.CustomerMapper;
import Mocks.Repositories.FakeCustomerRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerResourceTest extends JerseyTest {

    private FakeCustomerRepository repository;

    @Override
    protected Application configure() {
        repository = new FakeCustomerRepository();
        ICustomerService customerService = new CustomerService(repository, new CustomerMapper());

        ResourceConfig config = new ResourceConfig(CustomerResource.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(customerService).to(ICustomerService.class);
            }
        });
        config.register(GenericExceptionMapper.class);

        return config;
    }

    // -------------------------------------------------------------------------
    // GET /customers
    // -------------------------------------------------------------------------

    @Test
    void givenNoCustomers_whenList_thenReturns200AndEmptyList() {
        Response response = target("/customers").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertTrue(response.readEntity(List.class).isEmpty());
    }

    @Test
    void givenCustomers_whenList_thenReturns200AndList() {
        repository.add(buildCustomer("John", "Doe", "john@example.com"));
        repository.add(buildCustomer("Jane", "Doe", "jane@example.com"));

        Response response = target("/customers").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(2, response.readEntity(List.class).size());
    }

    // -------------------------------------------------------------------------
    // GET /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenGet_thenReturns200() {
        Customer customer = buildCustomer("John", "Doe", "john@example.com");
        repository.add(customer);

        Response response = target("/customers/" + customer.getId()).request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenNonExistingId_whenGet_thenReturns404() {
        Response response = target("/customers/" + UUID.randomUUID()).request().get();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // GET /customers/email/{email}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingEmail_whenGetByEmail_thenReturns200() {
        repository.add(buildCustomer("John", "Doe", "john@example.com"));

        Response response = target("/customers/email/john@example.com").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenNonExistingEmail_whenGetByEmail_thenReturns404() {
        Response response = target("/customers/email/unknown@example.com").request().get();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // POST /customers
    // -------------------------------------------------------------------------

    @Test
    void givenValidRequest_whenCreate_thenReturns201AndLocationHeader() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com");

        Response response = target("/customers")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        assertNotNull(response.getHeaderString("Location"));
        assertTrue(response.getHeaderString("Location").contains("/customers/"));
    }

    @Test
    void givenValidRequest_whenCreate_thenCustomerIsPersisted() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com");

        Response response = target("/customers")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        assertNotNull(repository.findByEmail("john@example.com"));
    }

    @Test
    void givenInvalidEmail_whenCreate_thenReturns400() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "invalid-email");

        Response response = target("/customers")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    @Test
    void givenDuplicateEmail_whenCreate_thenReturns400() {
        repository.add(buildCustomer("John", "Doe", "john@example.com"));
        CustomerRequest request = new CustomerRequest("Jane", "Doe", "john@example.com");

        Response response = target("/customers")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    @Test
    void givenNullRequest_whenCreate_thenReturns400() {
        Response response = target("/customers")
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // PUT /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenUpdate_thenReturns200() {
        Customer customer = buildCustomer("John", "Doe", "john@example.com");
        repository.add(customer);
        CustomerRequest request = new CustomerRequest("Johnny", "Doe", "johnny@example.com");

        Response response = target("/customers/" + customer.getId())
                .request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenExistingId_whenUpdate_thenCustomerIsUpdated() {
        Customer customer = buildCustomer("John", "Doe", "john@example.com");
        repository.add(customer);
        CustomerRequest request = new CustomerRequest("Johnny", "Doe", "johnny@example.com");

        target("/customers/" + customer.getId())
                .request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals("Johnny", repository.findById(customer.getId()).getFirstName());
        assertEquals("johnny@example.com", repository.findById(customer.getId()).getEmail());
    }

    @Test
    void givenNonExistingId_whenUpdate_thenReturns404() {
        CustomerRequest request = new CustomerRequest("Johnny", "Doe", "johnny@example.com");

        Response response = target("/customers/" + UUID.randomUUID())
                .request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // DELETE /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenReturns204() {
        Customer customer = buildCustomer("John", "Doe", "john@example.com");
        repository.add(customer);

        Response response = target("/customers/" + customer.getId()).request().delete();

        assertEquals(204, response.getStatus());
        assertNull(repository.findById(customer.getId()));
    }

    @Test
    void givenNonExistingId_whenDelete_thenReturns404() {
        Response response = target("/customers/" + UUID.randomUUID()).request().delete();

        assertEquals(404, response.getStatus());
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
