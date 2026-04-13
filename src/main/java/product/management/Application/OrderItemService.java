package product.management.Application;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Domain.DTO.OrderItem.OrderItemDTO;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;
import product.management.Infrastructure.Mappers.OrderItemMapper;
import product.management.Infrastructure.Repositories.OrderItemRepository;

import java.util.List;
import java.util.UUID;

@Singleton
public class OrderItemService {

    private final OrderItemRepository repository;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderItemMapper mapper;

    @Inject
    public OrderItemService(OrderItemRepository repository, ProductService productService, OrderService orderService, OrderItemMapper mapper) {
        this.repository = repository;
        this.productService = productService;
        this.orderService = orderService;
        this.mapper = mapper;
    }

    public List<OrderItemDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public OrderItemDTO findById(UUID id) {
        OrderItemDTO orderItem = mapper.toDto(repository.findById(id));
        if (orderItem == null) {
            throw new ElementNotFoundException("OrderItem with id: " + id + ": doesn't exist.");
        }
        return orderItem;
    }

    public List<OrderItemDTO> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<OrderItemDTO> findByProductId(UUID productId) {
        return repository.findByProductId(productId).stream()
                .map(mapper::toDto)
                .toList();
    }

    public void delete(UUID id) {
        findById(id);
        repository.delete(id);
    }

    public void deleteByOrderId(UUID orderId) {
        repository.deleteByOrderId(orderId);
    }

    public OrderItemDTO save(OrderItemRequest request) {
        if (productService.findById(request.productId()) == null) {
            throw new ElementNotFoundException("Product with id: " + request.productId() + ": doesn't exist.");
        }
        if (orderService.findById(request.orderId()) == null) {
            throw new ElementNotFoundException("Order with id: " + request.orderId() + ": doesn't exist.");
        }
        if (request == null) {
            throw new IllegalArgumentException("The request must not be empty");
        }
        return mapper.toDto(repository.save(request));
    }

    public OrderItemDTO save(UUID id, OrderItemRequest request) {
        findById(id);
        return mapper.toDto(repository.save(id, request));
    }
}
