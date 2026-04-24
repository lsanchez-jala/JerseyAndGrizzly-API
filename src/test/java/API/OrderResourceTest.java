package API;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.management.API.OrderResource;
import product.management.Application.IOrderService;
import product.management.Application.IShipmentService;
import product.management.Application.mocks.FakeKafkaProducerService;
import product.management.Application.exception.GenericExceptionMapper;
import product.management.Application.impl.CustomerService;
import product.management.Application.impl.OrderServiceImpl;
import product.management.Application.impl.ShipmentService;
import product.management.Domain.DTO.Order.OrderCreateRequest;
import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Enums.ShipmentStatus;
import product.management.Domain.Models.Customer;
import product.management.Domain.Models.Order;
import product.management.Domain.Models.Shipment;
import product.management.Infrastructure.Mappers.CustomerMapper;
import product.management.Infrastructure.Mappers.OrderMapper;
import product.management.Infrastructure.Mappers.ShipmentMapper;
import product.management.Infrastructure.Repositories.mocks.FakeCustomerRepository;
import product.management.Infrastructure.Repositories.mocks.FakeOrderRepository;
import product.management.Infrastructure.Repositories.mocks.FakeShipmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderResourceTest extends JerseyTest {

    private FakeOrderRepository orderRepository;
    private FakeCustomerRepository customerRepository;
    private FakeShipmentRepository shipmentRepository;
    private FakeKafkaProducerService kafkaService;
    private IOrderService orderService;

    @Override
    protected Application configure() {
        orderRepository    = new FakeOrderRepository();
        customerRepository = new FakeCustomerRepository();
        shipmentRepository = new FakeShipmentRepository();
        kafkaService       = new FakeKafkaProducerService();

        CustomerService customerService = new CustomerService(customerRepository, new CustomerMapper());
        IShipmentService shipmentService = new ShipmentService(shipmentRepository, new ShipmentMapper(), kafkaService);

        orderService = new OrderServiceImpl(
                orderRepository,
                kafkaService,
                new OrderMapper(),
                customerService,
                shipmentService
        );

        ResourceConfig config = new ResourceConfig(OrderResource.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(orderService).to(IOrderService.class);
            }
        });
        config.register(GenericExceptionMapper.class);

        return config;
    }

    @BeforeEach
    void reset() {
        orderRepository.getStore().forEach((id, order) -> orderRepository.getStore());
        kafkaService.reset();
    }

    // -------------------------------------------------------------------------
    // GET /orders
    // -------------------------------------------------------------------------

    @Test
    void givenNoOrders_whenList_thenReturns200AndEmptyList() {
        Response response = target("/orders").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertTrue(response.readEntity(List.class).isEmpty());
    }

    @Test
    void givenOrders_whenList_thenReturns200AndList() {
        orderRepository.add(buildOrder(null, null, OrderStatus.CREATED));
        orderRepository.add(buildOrder(null, null, OrderStatus.CREATED));

        Response response = target("/orders").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(2, response.readEntity(List.class).size());
    }

    // -------------------------------------------------------------------------
    // GET /orders/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenGet_thenReturns200() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);

        Response response = target("/orders/" + order.getId()).request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenNonExistingId_whenGet_thenReturns404() {
        Response response = target("/orders/" + UUID.randomUUID()).request().get();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // GET /orders/customer/{customerId}
    // -------------------------------------------------------------------------

    @Test
    void givenCustomerIdWithOrders_whenListByCustomer_thenReturns200AndList() {
        UUID customerId = UUID.randomUUID();
        orderRepository.add(buildOrder(customerId, null, OrderStatus.CREATED));
        orderRepository.add(buildOrder(customerId, null, OrderStatus.CREATED));

        Response response = target("/orders/customer/" + customerId).request().get();

        assertEquals(200, response.getStatus());
        assertEquals(2, response.readEntity(List.class).size());
    }

    @Test
    void givenCustomerIdWithNoOrders_whenListByCustomer_thenReturns200AndEmptyList() {
        Response response = target("/orders/customer/" + UUID.randomUUID()).request().get();

        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(List.class).isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /orders/shipment/{shipmentId}
    // -------------------------------------------------------------------------

    @Test
    void givenShipmentIdWithOrders_whenListByShipment_thenReturns200AndList() {
        UUID shipmentId = UUID.randomUUID();
        orderRepository.add(buildOrder(null, shipmentId, OrderStatus.CREATED));

        Response response = target("/orders/shipment/" + shipmentId).request().get();

        assertEquals(200, response.getStatus());
        assertEquals(1, response.readEntity(List.class).size());
    }

    @Test
    void givenShipmentIdWithNoOrders_whenListByShipment_thenReturns200AndEmptyList() {
        Response response = target("/orders/shipment/" + UUID.randomUUID()).request().get();

        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(List.class).isEmpty());
    }

    // -------------------------------------------------------------------------
    // POST /orders
    // -------------------------------------------------------------------------

    @Test
    void givenValidRequest_whenCreate_thenReturns201AndLocationHeader() {
        OrderCreateRequest request = new OrderCreateRequest(null, null);

        Response response = target("/orders")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        assertNotNull(response.getHeaderString("Location"));
        assertTrue(response.getHeaderString("Location").contains("/orders/"));
    }

    @Test
    void givenValidRequest_whenCreate_thenOrderIsPersisted() {
        OrderCreateRequest request = new OrderCreateRequest(null, null);

        Response response = target("/orders")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        assertEquals(1, orderRepository.getStore().size());
    }

    @Test
    void givenValidRequest_whenCreate_thenKafkaEventIsSent() {
        OrderCreateRequest request = new OrderCreateRequest(null, null);

        target("/orders")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertTrue(kafkaService.wasCalled());
    }

    @Test
    void givenRequestWithNonExistingCustomer_whenCreate_thenReturns404() {
        OrderCreateRequest request = new OrderCreateRequest(UUID.randomUUID(), null);

        Response response = target("/orders")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());
    }

    @Test
    void givenRequestWithNonExistingShipment_whenCreate_thenReturns404() {
        OrderCreateRequest request = new OrderCreateRequest(null, UUID.randomUUID());

        Response response = target("/orders")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());
    }

    @Test
    void givenRequestWithExistingCustomer_whenCreate_thenReturns201() {
        Customer customer = buildCustomer();
        customerRepository.add(customer);
        OrderCreateRequest request = new OrderCreateRequest(customer.getId(), null);

        Response response = target("/orders")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
    }

    @Test
    void givenRequestWithExistingShipment_whenCreate_thenReturns201() {
        Shipment shipment = buildShipment();
        shipmentRepository.add(shipment);
        OrderCreateRequest request = new OrderCreateRequest(null, shipment.getId());

        Response response = target("/orders")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // PATCH /orders/{id}
    // -------------------------------------------------------------------------

//    @Test
//    void givenExistingId_whenUpdate_thenReturns200() {
//        Order order = buildOrder(null, null, OrderStatus.CREATED);
//        orderRepository.add(order);
//        OrderCreateRequest request = new OrderCreateRequest(null, null);
//
//        Response response = target("/orders/" + order.getId())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(200, response.getStatus());
//    }
//
//    @Test
//    void givenNonExistingId_whenUpdate_thenReturns404() {
//        OrderCreateRequest request = new OrderCreateRequest(null, null);
//
//        Response response = target("/orders/" + UUID.randomUUID())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(404, response.getStatus());
//    }
//
//    @Test
//    void givenNonExistingCustomerInUpdate_whenUpdate_thenReturns404() {
//        Order order = buildOrder(null, null, OrderStatus.CREATED);
//        orderRepository.add(order);
//        OrderCreateRequest request = new OrderCreateRequest(UUID.randomUUID(), null);
//
//        Response response = target("/orders/" + order.getId())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(404, response.getStatus());
//    }

    // -------------------------------------------------------------------------
    // DELETE /orders/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenReturns204() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);

        Response response = target("/orders/" + order.getId()).request().delete();

        assertEquals(204, response.getStatus());
        assertNull(orderRepository.findById(order.getId()));
    }

    @Test
    void givenNonExistingId_whenDelete_thenReturns404() {
        Response response = target("/orders/" + UUID.randomUUID()).request().delete();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // PATCH /orders/{id}/status
    // -------------------------------------------------------------------------

//    @Test
//    void givenValidStatus_whenChangeStatus_thenReturns201AndLocationHeader() {
//        Order order = buildOrder(null, null, OrderStatus.CREATED);
//        orderRepository.add(order);
//        OrderStatusRequest request = new OrderStatusRequest("SHIPPED");
//
//        Response response = target("/orders/" + order.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(201, response.getStatus());
//        assertNotNull(response.getHeaderString("Location"));
//    }
//
//    @Test
//    void givenValidStatus_whenChangeStatus_thenKafkaEventIsSent() {
//        Order order = buildOrder(null, null, OrderStatus.CREATED);
//        orderRepository.add(order);
//        OrderStatusRequest request = new OrderStatusRequest("SHIPPED");
//        kafkaService.reset();
//
//        target("/orders/" + order.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertTrue(kafkaService.wasCalled());
//        assertTrue(kafkaService.wasCalledWith(order.getId().toString()));
//    }
//
//    @Test
//    void givenInvalidStatus_whenChangeStatus_thenReturns400() {
//        Order order = buildOrder(null, null, OrderStatus.CREATED);
//        orderRepository.add(order);
//        OrderStatusRequest request = new OrderStatusRequest("INVALID_STATUS");
//
//        Response response = target("/orders/" + order.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(400, response.getStatus());
//    }
//
//    @Test
//    void givenSameStatus_whenChangeStatus_thenReturns400() {
//        Order order = buildOrder(null, null, OrderStatus.CREATED);
//        orderRepository.add(order);
//        OrderStatusRequest request = new OrderStatusRequest("CREATED");
//
//        Response response = target("/orders/" + order.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(400, response.getStatus());
//    }
//
//    @Test
//    void givenNonExistingId_whenChangeStatus_thenReturns404() {
//        OrderStatusRequest request = new OrderStatusRequest("SHIPPED");
//
//        Response response = target("/orders/" + UUID.randomUUID() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(404, response.getStatus());
//    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Order buildOrder(UUID customerId, UUID shipmentId, OrderStatus status) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(customerId != null ? customerId : UUID.randomUUID());
        order.setShipmentId(shipmentId != null ? shipmentId : UUID.randomUUID());
        order.setStatus(status);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        return order;
    }

    private Customer buildCustomer() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        return customer;
    }

    private Shipment buildShipment() {
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setTrackingCode("TRK-001AA");
        shipment.setCarrier("FedEx");
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setCreatedAt(Instant.now());
        shipment.setUpdatedAt(Instant.now());
        return shipment;
    }
}