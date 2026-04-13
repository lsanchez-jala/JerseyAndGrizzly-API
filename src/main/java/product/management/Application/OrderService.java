package product.management.Application;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderRequest;
import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Order;
import product.management.Infrastructure.Mappers.OrderMapper;
import product.management.Infrastructure.Repositories.OrderRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Singleton
public class OrderService {

    private final OrderRepository repository;
    private final KafkaProducerService kafkaService;
    private final OrderMapper mapper;

    @Inject
    public OrderService(OrderRepository repository, KafkaProducerService kafkaService, OrderMapper mapper) {
        this.repository = repository;
        this.kafkaService = kafkaService;
        this.mapper = mapper;
    }

    public List<OrderDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public OrderDTO findById(UUID id) {
        OrderDTO order = mapper.toDto(repository.findById(id));
        if (order == null) {
            throw new ElementNotFoundException("Order with id: " + id + ": doesn't exist.");
        }
        return order;
    }

    public List<OrderDTO> findByCustomerId(UUID customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(mapper::toDto)
                .toList();
    }

    public void delete(UUID id) {
        findById(id);
        repository.delete(id);
    }

    public OrderDTO save(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("The request must not be empty");
        }
        Order newOrder = new Order();
        mapper.toEntity(request, newOrder);
        newOrder.setCreatedAt(Instant.now());
        newOrder.setUpdatedAt(Instant.now());
        newOrder.setStatus(OrderStatus.CREATED);
        var result = repository.save(newOrder);
        kafkaService.send(result.getId().toString(), result.toString());
        return mapper.toDto(repository.save(newOrder));
    }

    public OrderDTO save(UUID id, OrderRequest request) {
        findById(id);
        Order newOrder = new Order();
        mapper.toEntity(request, newOrder);
        return mapper.toDto(repository.save(id, newOrder));
    }

    public OrderDTO changeStatus(UUID orderId, OrderRequest request){
        findById(orderId);

        Order order = repository.updateStatus(orderId, request.status());
        kafkaService.send(orderId.toString(), order.toString());
        return mapper.toDto(order);
    }
}
