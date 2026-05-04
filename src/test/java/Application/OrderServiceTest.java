package Application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.management.Application.exception.BadRequestException;
import product.management.Application.exception.ElementNotFoundException;
import Mocks.Services.FakeKafkaProducerService;
import product.management.Application.impl.CustomerService;
import product.management.Application.impl.OrderServiceImpl;
import product.management.Application.impl.ShipmentService;
import product.management.Domain.DTO.Order.OrderCreateRequest;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderStatusRequest;
import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Customer;
import product.management.Domain.Models.Order;
import product.management.Domain.Models.Shipment;
import product.management.Infrastructure.Mappers.OrderMapper;
import Mocks.Repositories.FakeCustomerRepository;
import Mocks.Repositories.FakeOrderRepository;
import Mocks.Repositories.FakeShipmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    private FakeOrderRepository orderRepository;
    private FakeCustomerRepository customerRepository;
    private FakeShipmentRepository shipmentRepository;
    private FakeKafkaProducerService kafkaService;
    private CustomerService customerService;
    private ShipmentService shipmentService;
    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        orderRepository    = new FakeOrderRepository();
        customerRepository = new FakeCustomerRepository();
        shipmentRepository = new FakeShipmentRepository();
        kafkaService       = new FakeKafkaProducerService();

        customerService = new CustomerService(customerRepository, new product.management.Infrastructure.Mappers.CustomerMapper());
        shipmentService = new ShipmentService(shipmentRepository, new product.management.Infrastructure.Mappers.ShipmentMapper(), kafkaService);

        service = new OrderServiceImpl(
                orderRepository,
                kafkaService,
                new OrderMapper(),
                customerService,
                shipmentService
        );
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void givenNoOrders_whenFindAll_thenReturnsEmptyList() {
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void givenOrders_whenFindAll_thenReturnsAllMapped() {
        orderRepository.add(buildOrder(null, null, OrderStatus.CREATED));
        orderRepository.add(buildOrder(null, null, OrderStatus.CREATED));

        List<OrderDTO> result = service.findAll();

        assertEquals(2, result.size());
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenFindById_thenReturnsDTO() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);

        OrderDTO result = service.findById(order.getId());

        assertEquals(order.getId(), result.id());
    }

    @Test
    void givenNonExistingId_whenFindById_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.findById(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // findByCustomerId
    // -------------------------------------------------------------------------

    @Test
    void givenCustomerId_whenFindByCustomerId_thenReturnsMatchingOrders() {
        UUID customerId = UUID.randomUUID();
        orderRepository.add(buildOrder(customerId, null, OrderStatus.CREATED));
        orderRepository.add(buildOrder(customerId, null, OrderStatus.CREATED));
        orderRepository.add(buildOrder(null, null, OrderStatus.CREATED));

        List<OrderDTO> result = service.findByCustomerId(customerId);

        assertEquals(2, result.size());
    }

    @Test
    void givenCustomerIdWithNoOrders_whenFindByCustomerId_thenReturnsEmptyList() {
        assertTrue(service.findByCustomerId(UUID.randomUUID()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // findByShipmentId
    // -------------------------------------------------------------------------

    @Test
    void givenShipmentId_whenFindByShipmentId_thenReturnsMatchingOrders() {
        UUID shipmentId = UUID.randomUUID();
        orderRepository.add(buildOrder(null, shipmentId, OrderStatus.CREATED));
        orderRepository.add(buildOrder(null, null, OrderStatus.CREATED));

        List<OrderDTO> result = service.findByShipmentId(shipmentId);

        assertEquals(1, result.size());
    }

    @Test
    void givenShipmentIdWithNoOrders_whenFindByShipmentId_thenReturnsEmptyList() {
        assertTrue(service.findByShipmentId(UUID.randomUUID()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenOrderIsRemoved() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);

        service.delete(order.getId());

        assertNull(orderRepository.findById(order.getId()));
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
    void givenNullRequest_whenSave_thenOrderIsCreatedWithCreatedStatus() {
        OrderDTO result = service.save(null);

        assertNotNull(result.id());
        assertEquals(OrderStatus.CREATED.name(), result.status());
    }

    @Test
    void givenValidRequestWithoutCustomerOrShipment_whenSave_thenOrderIsPersisted() {
        OrderCreateRequest request = new OrderCreateRequest(null, null);

        OrderDTO result = service.save(request);

        assertNotNull(result.id());
        assertNotNull(orderRepository.findById(result.id()));
    }

    @Test
    void givenValidRequest_whenSave_thenKafkaEventIsSent() {
        OrderCreateRequest request = new OrderCreateRequest(null, null);

        OrderDTO result = service.save(request);

        assertTrue(kafkaService.wasCalled());
        assertTrue(kafkaService.wasCalledWith(result.id().toString()));
    }

    @Test
    void givenRequestWithExistingCustomer_whenSave_thenSucceeds() {
        Customer customer = buildCustomer("john@example.com");
        customerRepository.add(customer);
        OrderCreateRequest request = new OrderCreateRequest(customer.getId().toString(), null);

        assertDoesNotThrow(() -> service.save(request));
    }

    @Test
    void givenRequestWithNonExistingCustomer_whenSave_thenThrowsElementNotFoundException() {
        OrderCreateRequest request = new OrderCreateRequest(UUID.randomUUID().toString(), null);

        assertThrows(ElementNotFoundException.class, () -> service.save(request));
    }

    @Test
    void givenRequestWithExistingShipment_whenSave_thenSucceeds() {
        Shipment shipment = buildShipment();
        shipmentRepository.add(shipment);
        OrderCreateRequest request = new OrderCreateRequest(null, shipment.getId().toString());

        assertDoesNotThrow(() -> service.save(request));
    }

    @Test
    void givenRequestWithNonExistingShipment_whenSave_thenThrowsElementNotFoundException() {
        OrderCreateRequest request = new OrderCreateRequest(null, UUID.randomUUID().toString());

        assertThrows(ElementNotFoundException.class, () -> service.save(request));
    }

    @Test
    void givenRequestWithExistingCustomerAndShipment_whenSave_thenSucceeds() {
        Customer customer = buildCustomer("john@example.com");
        customerRepository.add(customer);
        Shipment shipment = buildShipment();
        shipmentRepository.add(shipment);
        OrderCreateRequest request = new OrderCreateRequest(customer.getId().toString(), shipment.getId().toString());

        assertDoesNotThrow(() -> service.save(request));
    }

    // -------------------------------------------------------------------------
    // save (update)
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenSaveWithId_thenOrderIsUpdated() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);
        Customer customer = buildCustomer("john@example.com");
        customerRepository.add(customer);
        OrderCreateRequest request = new OrderCreateRequest(customer.getId().toString(), null);

        OrderDTO result = service.save(order.getId(), request);

        assertEquals(customer.getId(), result.customerId());
    }

    @Test
    void givenNonExistingId_whenSaveWithId_thenThrowsElementNotFoundException() {
        OrderCreateRequest request = new OrderCreateRequest(null, null);

        assertThrows(ElementNotFoundException.class,
                () -> service.save(UUID.randomUUID(), request));
    }

    @Test
    void givenNonExistingCustomerInUpdate_whenSaveWithId_thenThrowsElementNotFoundException() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);
        OrderCreateRequest request = new OrderCreateRequest(UUID.randomUUID().toString(), null);

        assertThrows(ElementNotFoundException.class,
                () -> service.save(order.getId(), request));
    }

    @Test
    void givenNonExistingShipmentInUpdate_whenSaveWithId_thenThrowsElementNotFoundException() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);
        OrderCreateRequest request = new OrderCreateRequest(null, UUID.randomUUID().toString());

        assertThrows(ElementNotFoundException.class,
                () -> service.save(order.getId(), request));
    }

    // -------------------------------------------------------------------------
    // changeStatus
    // -------------------------------------------------------------------------

    @Test
    void givenValidStatus_whenChangeStatus_thenStatusIsUpdated() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);
        OrderStatusRequest request = new OrderStatusRequest("SHIPPED");

        OrderDTO result = service.changeStatus(order.getId(), request);

        assertEquals("SHIPPED", result.status());
    }

    @Test
    void givenValidStatus_whenChangeStatus_thenKafkaEventIsSent() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);
        OrderStatusRequest request = new OrderStatusRequest("SHIPPED");

        kafkaService.reset();
        service.changeStatus(order.getId(), request);

        assertTrue(kafkaService.wasCalled());
        assertTrue(kafkaService.wasCalledWith(order.getId().toString()));
    }

    @Test
    void givenNullRequest_whenChangeStatus_thenThrowsBadRequestException() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);

        assertThrows(BadRequestException.class,
                () -> service.changeStatus(order.getId(), null));
    }

    @Test
    void givenNullStatus_whenChangeStatus_thenThrowsBadRequestException() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);
        OrderStatusRequest request = new OrderStatusRequest(null);

        assertThrows(BadRequestException.class,
                () -> service.changeStatus(order.getId(), request));
    }

    @Test
    void givenInvalidStatus_whenChangeStatus_thenThrowsBadRequestException() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);
        OrderStatusRequest request = new OrderStatusRequest("INVALID_STATUS");

        assertThrows(BadRequestException.class,
                () -> service.changeStatus(order.getId(), request));
    }

    @Test
    void givenSameStatus_whenChangeStatus_thenThrowsBadRequestException() {
        Order order = buildOrder(null, null, OrderStatus.CREATED);
        orderRepository.add(order);
        OrderStatusRequest request = new OrderStatusRequest("CREATED");

        assertThrows(BadRequestException.class,
                () -> service.changeStatus(order.getId(), request));
    }

    @Test
    void givenNonExistingId_whenChangeStatus_thenThrowsElementNotFoundException() {
        OrderStatusRequest request = new OrderStatusRequest("SHIPPED");

        assertThrows(ElementNotFoundException.class,
                () -> service.changeStatus(UUID.randomUUID(), request));
    }

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

    private Customer buildCustomer(String email) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail(email);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        return customer;
    }

    private Shipment buildShipment() {
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setTrackingCode("TRK-001AA");
        shipment.setCarrier("FedEx");
        shipment.setStatus(product.management.Domain.Enums.ShipmentStatus.CREATED);
        shipment.setCreatedAt(Instant.now());
        shipment.setUpdatedAt(Instant.now());
        return shipment;
    }
}