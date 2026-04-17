package product.management.Application;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Application.exception.BadRequestException;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderCreateRequest;
import product.management.Domain.DTO.Order.OrderStatusRequest;
import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Order;
import product.management.Infrastructure.Mappers.OrderMapper;
import product.management.Infrastructure.Repositories.OrderRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Singleton
public class OrderService {

    private final OrderRepository repository;
    private final KafkaProducerService kafkaService;
    private final OrderMapper mapper;
    private final CustomerService customerService;
    private final ShipmentService shipmentService;

    @Inject
    public OrderService(OrderRepository repository, KafkaProducerService kafkaService, OrderMapper mapper, CustomerService customerService, ShipmentService shipmentService) {
        this.repository = repository;
        this.kafkaService = kafkaService;
        this.mapper = mapper;
        this.customerService = customerService;
        this.shipmentService = shipmentService;
    }

    public List<OrderDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public OrderDTO findById(UUID id) {
        Order order = repository.findById(id);
        if (order == null) {
            throw new ElementNotFoundException("Order with id: " + id + " was NOT FOUND.");
        }
        return  mapper.toDto(order);
    }

    public List<OrderDTO> findByCustomerId(UUID customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<OrderDTO> findByShipmentId(UUID shipmentId) {
        return repository.findByShipmentId(shipmentId).stream()
                .map(mapper::toDto)
                .toList();
    }

    public void delete(UUID id) {
        findById(id);
        repository.delete(id);
    }

    public OrderDTO save(OrderCreateRequest request) {
        if (request != null && request.shipmentId() != null ){
            shipmentService.findById(request.shipmentId());
        }
        if (request != null && request.customerId() != null){
            customerService.findById(request.customerId());
        }

        Order newOrder = new Order();
        if (request != null ){
            mapper.toEntity(request, newOrder);
        }
        newOrder.setStatus(OrderStatus.CREATED);
        OrderDTO result = mapper.toDto(repository.save(newOrder));
        kafkaService.send(result.id().toString(), mapper.toGenericRecord(result));
        return result;
    }

    public OrderDTO save(UUID id, OrderCreateRequest request) {
        findById(id);
        if (request.shipmentId() != null ){
            shipmentService.findById(request.shipmentId());
        }
        if (request.customerId() != null){
            customerService.findById(request.customerId());
        }
        Order newOrder = new Order();
        mapper.toEntity(request, newOrder);
        return mapper.toDto(repository.save(id, newOrder));
    }

    public OrderDTO changeStatus(UUID orderId, OrderStatusRequest request){
        OrderDTO prev = findById(orderId);
        if (request == null) {
            throw new BadRequestException("The request must not be empty");
        }
        if (request.status() == null){
            throw new BadRequestException("The status must not be empty");
        }
        if (!OrderStatus.isValid(request.status())){
            throw new BadRequestException("Invalid status. Accepted values are: " + Arrays.toString(OrderStatus.values()));
        }
        if (Objects.equals(prev.status(), request.status())){
            throw new BadRequestException("Status: "+request.status()+" already assigned.");
        }
        OrderDTO order = mapper.toDto(repository.updateStatus(orderId, OrderStatus.valueOf(request.status())));
        kafkaService.send(orderId.toString(), mapper.toGenericRecord(order));
        return order;
    }
}
