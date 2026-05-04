package API;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import product.management.API.OrderItemResource;
import product.management.Application.IOrderItemService;
import product.management.Application.IOrderService;
import product.management.Application.IProductService;
import product.management.Application.exception.GenericExceptionMapper;
import product.management.Application.impl.CustomerService;
import product.management.Application.impl.OrderItemService;
import product.management.Application.impl.OrderServiceImpl;
import product.management.Application.impl.ProductService;
import product.management.Application.impl.ShipmentService;
import Mocks.Services.FakeKafkaProducerService;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;
import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Order;
import product.management.Domain.Models.Product;
import product.management.Infrastructure.Mappers.CustomerMapper;
import product.management.Infrastructure.Mappers.OrderItemMapper;
import product.management.Infrastructure.Mappers.OrderMapper;
import product.management.Infrastructure.Mappers.ProductMapper;
import product.management.Infrastructure.Mappers.ShipmentMapper;
import Mocks.Repositories.FakeCustomerRepository;
import Mocks.Repositories.FakeOrderItemRepository;
import Mocks.Repositories.FakeOrderRepository;
import Mocks.Repositories.FakeProductRepository;
import Mocks.Repositories.FakeShipmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemResourceTest extends JerseyTest {

    private FakeOrderItemRepository orderItemRepository;
    private FakeOrderRepository orderRepository;
    private FakeProductRepository productRepository;

    @Override
    protected Application configure() {
        orderItemRepository = new FakeOrderItemRepository();
        orderRepository     = new FakeOrderRepository();
        productRepository   = new FakeProductRepository();

        FakeCustomerRepository customerRepository  = new FakeCustomerRepository();
        IOrderItemService orderItemService = getIOrderItemService(customerRepository);

        ResourceConfig config = new ResourceConfig(OrderItemResource.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(orderItemService).to(IOrderItemService.class);
            }
        });
        config.register(GenericExceptionMapper.class);

        return config;
    }

    @NotNull
    private IOrderItemService getIOrderItemService(FakeCustomerRepository customerRepository) {
        FakeShipmentRepository shipmentRepository  = new FakeShipmentRepository();
        FakeKafkaProducerService kafkaService      = new FakeKafkaProducerService();

        IProductService productService = new ProductService(
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

        IOrderService orderService = new OrderServiceImpl(
                orderRepository,
                kafkaService,
                new OrderMapper(),
                customerService,
                shipmentService
        );

        IOrderItemService orderItemService = new OrderItemService(
                orderItemRepository,
                productService,
                orderService,
                new OrderItemMapper()
        );
        return orderItemService;
    }

    // -------------------------------------------------------------------------
    // GET /order-items
    // -------------------------------------------------------------------------

    @Test
    void givenNoOrderItems_whenList_thenReturns200AndEmptyList() {
        Response response = target("/order-items").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertTrue(response.readEntity(List.class).isEmpty());
    }

    @Test
    void givenOrderItems_whenList_thenReturns200AndList() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        orderItemRepository.add(buildOrderItem(orderId, productId));
        orderItemRepository.add(buildOrderItem(orderId, productId));

        Response response = target("/order-items").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(2, response.readEntity(List.class).size());
    }

    // -------------------------------------------------------------------------
    // GET /order-items/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenGet_thenReturns200() {
        product.management.Domain.Models.OrderItem item = buildOrderItem(addOrder(), addProduct());
        orderItemRepository.add(item);

        Response response = target("/order-items/" + item.getId()).request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenNonExistingId_whenGet_thenReturns404() {
        Response response = target("/order-items/" + UUID.randomUUID()).request().get();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // GET /order-items/order/{orderId}
    // -------------------------------------------------------------------------

    @Test
    void givenOrderIdWithItems_whenListByOrderId_thenReturns200AndList() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        orderItemRepository.add(buildOrderItem(orderId, productId));
        orderItemRepository.add(buildOrderItem(orderId, productId));

        Response response = target("/order-items/order/" + orderId).request().get();

        assertEquals(200, response.getStatus());
        assertEquals(2, response.readEntity(List.class).size());
    }

    @Test
    void givenOrderIdWithNoItems_whenListByOrderId_thenReturns200AndEmptyList() {
        Response response = target("/order-items/order/" + UUID.randomUUID()).request().get();

        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(List.class).isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /order-items/product/{productId}
    // -------------------------------------------------------------------------

    @Test
    void givenProductIdWithItems_whenListByProductId_thenReturns200AndList() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        orderItemRepository.add(buildOrderItem(orderId, productId));
        orderItemRepository.add(buildOrderItem(orderId, productId));

        Response response = target("/order-items/product/" + productId).request().get();

        assertEquals(200, response.getStatus());
        assertEquals(2, response.readEntity(List.class).size());
    }

    @Test
    void givenProductIdWithNoItems_whenListByProductId_thenReturns200AndEmptyList() {
        Response response = target("/order-items/product/" + UUID.randomUUID()).request().get();

        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(List.class).isEmpty());
    }

    // -------------------------------------------------------------------------
    // POST /order-items
    // -------------------------------------------------------------------------

    @Test
    void givenValidRequest_whenCreate_thenReturns201AndLocationHeader() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        OrderItemRequest request = new OrderItemRequest(orderId, productId, 2, 99.99f);

        Response response = target("/order-items")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        assertNotNull(response.getHeaderString("Location"));
        assertTrue(response.getHeaderString("Location").contains("/order-items/"));
    }

    @Test
    void givenValidRequest_whenCreate_thenOrderItemIsPersisted() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        OrderItemRequest request = new OrderItemRequest(orderId, productId, 2, 99.99f);

        target("/order-items")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(1, orderItemRepository.getStore().size());
    }

    @Test
    void givenNonExistingOrderId_whenCreate_thenReturns404() {
        UUID productId = addProduct();
        OrderItemRequest request = new OrderItemRequest(UUID.randomUUID(), productId, 2, 99.99f);

        Response response = target("/order-items")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());
    }

    @Test
    void givenNonExistingProductId_whenCreate_thenReturns404() {
        UUID orderId = addOrder();
        OrderItemRequest request = new OrderItemRequest(orderId, UUID.randomUUID(), 2, 99.99f);

        Response response = target("/order-items")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());
    }

    @Test
    void givenNullOrderId_whenCreate_thenReturns400() {
        OrderItemRequest request = new OrderItemRequest(null, UUID.randomUUID(), 2, 99.99f);

        Response response = target("/order-items")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    @Test
    void givenNullProductId_whenCreate_thenReturns400() {
        OrderItemRequest request = new OrderItemRequest(UUID.randomUUID(), null, 2, 99.99f);

        Response response = target("/order-items")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // PUT /order-items/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenUpdate_thenReturns200() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        product.management.Domain.Models.OrderItem item = buildOrderItem(orderId, productId);
        orderItemRepository.add(item);
        OrderItemRequest request = new OrderItemRequest(orderId, productId, 5, 49.99f);

        Response response = target("/order-items/" + item.getId())
                .request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenExistingId_whenUpdate_thenOrderItemIsUpdated() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        product.management.Domain.Models.OrderItem item = buildOrderItem(orderId, productId);
        orderItemRepository.add(item);
        OrderItemRequest request = new OrderItemRequest(orderId, productId, 5, 49.99f);

        target("/order-items/" + item.getId())
                .request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(5, orderItemRepository.findById(item.getId()).getQuantity());
    }

    @Test
    void givenNonExistingId_whenUpdate_thenReturns404() {
        OrderItemRequest request = new OrderItemRequest(UUID.randomUUID(), UUID.randomUUID(), 2, 99.99f);

        Response response = target("/order-items/" + UUID.randomUUID())
                .request()
                .put(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // DELETE /order-items/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenReturns204() {
        product.management.Domain.Models.OrderItem item = buildOrderItem(addOrder(), addProduct());
        orderItemRepository.add(item);

        Response response = target("/order-items/" + item.getId()).request().delete();

        assertEquals(204, response.getStatus());
        assertNull(orderItemRepository.findById(item.getId()));
    }

    @Test
    void givenNonExistingId_whenDelete_thenReturns404() {
        Response response = target("/order-items/" + UUID.randomUUID()).request().delete();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // DELETE /order-items/order/{orderId}
    // -------------------------------------------------------------------------

    @Test
    void givenOrderIdWithItems_whenDeleteByOrderId_thenReturns204() {
        UUID orderId   = addOrder();
        UUID productId = addProduct();
        orderItemRepository.add(buildOrderItem(orderId, productId));
        orderItemRepository.add(buildOrderItem(orderId, productId));

        Response response = target("/order-items/order/" + orderId).request().delete();

        assertEquals(204, response.getStatus());
        assertTrue(orderItemRepository.findByOrderId(orderId).isEmpty());
    }

    @Test
    void givenOrderIdWithNoItems_whenDeleteByOrderId_thenReturns204() {
        Response response = target("/order-items/order/" + UUID.randomUUID()).request().delete();

        assertEquals(204, response.getStatus());
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

    private product.management.Domain.Models.OrderItem buildOrderItem(UUID orderId, UUID productId) {
        product.management.Domain.Models.OrderItem item = new product.management.Domain.Models.OrderItem();
        item.setId(UUID.randomUUID());
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(1);
        item.setUnitPrice(10.0f);
        return item;
    }
}