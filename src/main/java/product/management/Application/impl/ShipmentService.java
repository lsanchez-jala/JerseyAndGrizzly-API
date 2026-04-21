package product.management.Application.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import product.management.Application.IKafkaProducerService;
import product.management.Application.IShipmentService;
import product.management.Application.exception.BadRequestException;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;
import product.management.Domain.Enums.ShipmentStatus;
import product.management.Domain.Models.Shipment;
import product.management.Infrastructure.Mappers.ShipmentMapper;
import product.management.Infrastructure.Repositories.ShipmentRepository;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Singleton
public class ShipmentService implements IShipmentService {

    private final ShipmentRepository repository;
    private final ShipmentMapper mapper;
    private final IKafkaProducerService kafkaService;

    @Inject
    public ShipmentService(ShipmentRepository repository, ShipmentMapper mapper, IKafkaProducerService kafkaService) {
        this.repository = repository;
        this.mapper = mapper;
        this.kafkaService = kafkaService;
    }

    public List<ShipmentDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public ShipmentDTO findById(UUID id) {
        Shipment shipment = repository.findById(id);
        if (shipment == null) {
            throw new ElementNotFoundException("Shipment with id: " + id + ": was NOT FOUND.");
        }
        return mapper.toDto(shipment);
    }

    public ShipmentDTO findByTrackingCode(String trackingCode) {
        Shipment shipment = repository.findByTrackingCode(trackingCode);
        if (shipment == null) {
            throw new ElementNotFoundException("Shipment with tracking code: " + trackingCode + ": was NOT FOUND.");
        }
        return mapper.toDto(shipment);
    }

    public void delete(UUID id) {
        findById(id);
        repository.delete(id);
    }

    public ShipmentDTO save(ShipmentRequest request) {
        if (request == null) {
            throw new BadRequestException("The request must not be empty");
        }
        if (request.carrier().isEmpty()){
            throw new BadRequestException("The carrier must not be empty");
        }

        Shipment shipment = new Shipment();
        mapper.toEntity(request, shipment);

        shipment.setTrackingCode(createNewTrackingCode());

        shipment.setStatus(ShipmentStatus.CREATED);
        return mapper.toDto(repository.save(shipment));
    }

    private @NotNull String createNewTrackingCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();

        String digits = IntStream.range(0, 6)
                .mapToObj(i -> String.valueOf(random.nextInt(10)))
                .collect(Collectors.joining());

        String letters = IntStream.range(0, 2)
                .mapToObj(i -> String.valueOf(chars.charAt(random.nextInt(26))))
                .collect(Collectors.joining());

        return "TRK-" + digits + letters;
    }

    public ShipmentDTO save(UUID id, ShipmentRequest request) {
        findById(id);
        if (request == null) {
            throw new BadRequestException("The request must not be empty");
        }

        Shipment shipment = new Shipment();
        mapper.toEntity(request, shipment);
        return mapper.toDto(repository.save(id, shipment));
    }

    public ShipmentDTO changeStatus(UUID shipmentId, ShipmentRequest request){
        ShipmentDTO prev = findById(shipmentId);
        if (request == null) {
            throw new BadRequestException("The request must not be empty");
        }
        if (request.status() == null){
            throw new BadRequestException("The status must not be empty");
        }
        if (!ShipmentStatus.isValid(request.status())){
            throw new BadRequestException("Invalid status. Accepted values are: " + Arrays.toString(ShipmentStatus.values()));
        }
        if (Objects.equals(prev.status(), request.status())){
            throw new BadRequestException("Status: "+request.status()+" already assigned.");
        }
        ShipmentDTO shipment = mapper.toDto(repository.updateStatus(shipmentId, ShipmentStatus.valueOf(request.status())));
        kafkaService.send(shipmentId.toString(), mapper.toGenericRecord(shipment));
        return shipment;
    }
}