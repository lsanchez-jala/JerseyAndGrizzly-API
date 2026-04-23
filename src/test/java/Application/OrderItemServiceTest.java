package Application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.management.Application.IOrderService;
import product.management.Application.IProductService;
import product.management.Application.exception.BadRequestException;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Application.Fakes.FakeKafkaProducerService;
import product.management.Application.impl.*;
import product.management.Domain.DTO.OrderItem.OrderItemDTO;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;
import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Order;
import product.management.Domain.Models.OrderItem;
import product.management.Domain.Models.Product;
import product.management.Infrastructure.Mappers.CustomerMapper;
import product.management.Infrastructure.Mappers.OrderItemMapper;
import product.management.Infrastructure.Mappers.OrderMapper;
import product.management.Infrastructure.Mappers.ProductMapper;
import product.management.Infrastructure.Mappers.ShipmentMapper;
import product.management.Infrastructure.Repositories.Fakes.FakeCustomerRepository;
import product.management.Infrastructure.Repositories.Fakes.FakeOrderItemRepository;
import product.management.Infrastructure.Repositories.Fakes.FakeOrderRepository;
import product.management.Infrastructure.Repositories.Fakes.FakeProductRepository;
import product.management.Infrastructure.Repositories.Fakes.FakeShipmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderItemServiceTest {

    private FakeOrderItemRepository repository;
    private FakeOrderRepository orderRepository;
    private FakeProductRepository productRepository;
    private IProductService productService;
    private IOrderService orderService;
    private OrderItemService service;

    @BeforeEach
    void setUp() {
        repository        = new FakeOrderItemRepository();
        orderRepository   = new FakeOrderRepository();
        productRepository = new FakeProductRepository();

        FakeCustomerRepository customerRepository = new FakeCustomerRepository();
        FakeShipmentRepository shipmentRepository = new FakeShipmentRepository();
        FakeKafkaProducerService kafkaService     = new FakeKafkaProducerService();

        productService = new ProductService(
                productRepository,
                new ProductMapper()
        );

        CustomerService customerService = new CustomerService(
                customerRepository,
                new CustomerMapper()
        );

        ShipmentService shipmentService = new ShipmentService(
                shipmentRepository,
                new ShipmentMapper(),
                kafkaService
        );

        orderService = new OrderServiceImpl(
                orderRepository,
                kafkaService,
                new OrderMapper(),
                customerService,
                shipmentService
        );

        service = new OrderItemService(
                repository,
                productService,
                orderService,
                new OrderItemMapper()
        );
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void givenNoOrderItems_whenFindAll_thenReturnsEmptyList() {
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void givenOrderItems_whenFindAll_thenReturnsAllMapped() {
        repository.add(buildOrderItem(UUID.randomUUID(), UUID.randomUUID()));
        repository.add(buildOrderItem(UUID.randomUUID(), UUID.randomUUID()));

        List<OrderItemDTO> result = service.findAll();

        assertEquals(2, result.size());
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenFindById_thenReturnsDTO() {
        OrderItem item = buildOrderItem(UUID.randomUUID(), UUID.randomUUID());
        repository.add(item);

        OrderItemDTO result = service.findById(item.getId());

        assertEquals(item.getId(), result.id());
    }

    @Test
    void givenNonExistingId_whenFindById_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.findById(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // findByOrderId
    // -------------------------------------------------------------------------

    @Test
    void givenOrderIdWithItems_whenFindByOrderId_thenReturnsMatchingItems() {
        UUID orderId = UUID.randomUUID();
        repository.add(buildOrderItem(orderId, UUID.randomUUID()));
        repository.add(buildOrderItem(orderId, UUID.randomUUID()));
        repository.add(buildOrderItem(UUID.randomUUID(), UUID.randomUUID()));

        List<OrderItemDTO> result = service.findByOrderId(orderId);

        assertEquals(2, result.size());
    }

    @Test
    void givenOrderIdWithNoItems_whenFindByOrderId_thenReturnsEmptyList() {
        assertTrue(service.findByOrderId(UUID.randomUUID()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // findByProductId
    // -------------------------------------------------------------------------

    @Test
    void givenProductIdWithItems_whenFindByProductId_thenReturnsMatchingItems() {
        UUID productId = UUID.randomUUID();
        repository.add(buildOrderItem(UUID.randomUUID(), productId));
        repository.add(buildOrderItem(UUID.randomUUID(), productId));
        repository.add(buildOrderItem(UUID.randomUUID(), UUID.randomUUID()));

        List<OrderItemDTO> result = service.findByProductId(productId);

        assertEquals(2, result.size());
    }

    @Test
    void givenProductIdWithNoItems_whenFindByProductId_thenReturnsEmptyList() {
        assertTrue(service.findByProductId(UUID.randomUUID()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenOrderItemIsRemoved() {
        OrderItem item = buildOrderItem(UUID.randomUUID(), UUID.randomUUID());
        repository.add(item);

        service.delete(item.getId());

        assertNull(repository.findById(item.getId()));
    }

    @Test
    void givenNonExistingId_whenDelete_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.delete(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // deleteByOrderId
    // -------------------------------------------------------------------------

    @Test
    void givenOrderIdWithItems_whenDeleteByOrderId_thenAllItemsAreRemoved() {
        UUID orderId = UUID.randomUUID();
        repository.add(buildOrderItem(orderId, UUID.randomUUID()));
        repository.add(buildOrderItem(orderId, UUID.randomUUID()));

        service.deleteByOrderId(orderId);

        assertTrue(repository.findByOrderId(orderId).isEmpty());
    }

    @Test
    void givenOrderIdWithNoItems_whenDeleteByOrderId_thenNothingHappens() {
        assertDoesNotThrow(() -> service.deleteByOrderId(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // save (create)
    // -------------------------------------------------------------------------

    @Test
    void givenValidRequest_whenSave_thenOrderItemIsPersisted() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        OrderItemRequest request = new OrderItemRequest(orderId, productId, 2, 99.99f);

        OrderItemDTO result = service.save(request);

        assertNotNull(result.id());
        assertNotNull(repository.findById(result.id()));
        assertEquals(orderId, result.orderId());
        assertEquals(productId, result.productId());
        assertEquals(2, result.quantity());
    }

    @Test
    void givenNullRequest_whenSave_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.save((OrderItemRequest) null));
    }

    @Test
    void givenNullOrderId_whenSave_thenThrowsBadRequestException() {
        OrderItemRequest request = new OrderItemRequest(null, UUID.randomUUID(), 2, 99.99f);

        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenNullProductId_whenSave_thenThrowsBadRequestException() {
        OrderItemRequest request = new OrderItemRequest(UUID.randomUUID(), null, 2, 99.99f);

        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenNonExistingOrderId_whenSave_thenThrowsElementNotFoundException() {
        UUID productId = addProduct();
        OrderItemRequest request = new OrderItemRequest(UUID.randomUUID(), productId, 2, 99.99f);

        assertThrows(ElementNotFoundException.class, () -> service.save(request));
    }

    @Test
    void givenNonExistingProductId_whenSave_thenThrowsElementNotFoundException() {
        UUID orderId = addOrder();
        OrderItemRequest request = new OrderItemRequest(orderId, UUID.randomUUID(), 2, 99.99f);

        assertThrows(ElementNotFoundException.class, () -> service.save(request));
    }

    // -------------------------------------------------------------------------
    // save (update)
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenSaveWithId_thenOrderItemIsUpdated() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        OrderItem item = buildOrderItem(orderId, productId);
        repository.add(item);
        OrderItemRequest request = new OrderItemRequest(orderId, productId, 5, 49.99f);

        OrderItemDTO result = service.save(item.getId(), request);

        assertEquals(5, result.quantity());
        assertEquals(49.99f, result.unitPrice());
    }

    @Test
    void givenNonExistingId_whenSaveWithId_thenThrowsElementNotFoundException() {
        OrderItemRequest request = new OrderItemRequest(UUID.randomUUID(), UUID.randomUUID(), 2, 99.99f);

        assertThrows(ElementNotFoundException.class,
                () -> service.save(UUID.randomUUID(), request));
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private UUID addOrder() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepository.add(order);
        return order.getId();
    }

    private UUID addProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setSku("SKU-" + UUID.randomUUID());
        product.setCategory("Electronics");
        product.setPrice(10.0F);
        product.setStock(100);
        productRepository.add(product);
        return product.getId();
    }

    private OrderItem buildOrderItem(UUID orderId, UUID productId) {
        OrderItem item = new OrderItem();
        item.setId(UUID.randomUUID());
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(1);
        item.setUnitPrice(10.0f);
        return item;
    }
}
