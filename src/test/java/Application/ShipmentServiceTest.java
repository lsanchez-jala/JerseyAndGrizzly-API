package Application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.management.Application.exception.BadRequestException;
import product.management.Application.exception.ElementNotFoundException;
import Mocks.Services.FakeKafkaProducerService;
import product.management.Application.impl.ShipmentService;
import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;
import product.management.Domain.Enums.ShipmentStatus;
import product.management.Domain.Models.Shipment;
import product.management.Infrastructure.Mappers.ShipmentMapper;
import Mocks.Repositories.FakeShipmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ShipmentServiceTest {

    private FakeShipmentRepository repository;
    private FakeKafkaProducerService kafkaService;
    private ShipmentService service;

    @BeforeEach
    void setUp() {
        repository   = new FakeShipmentRepository();
        kafkaService = new FakeKafkaProducerService();
        service      = new ShipmentService(repository, new ShipmentMapper(), kafkaService);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void givenNoShipments_whenFindAll_thenReturnsEmptyList() {
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void givenShipments_whenFindAll_thenReturnsAllMapped() {
        repository.add(buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED));
        repository.add(buildShipment("TRK-002BB", "DHL", ShipmentStatus.CREATED));

        List<ShipmentDTO> result = service.findAll();

        assertEquals(2, result.size());
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenFindById_thenReturnsDTO() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);

        ShipmentDTO result = service.findById(shipment.getId());

        assertEquals(shipment.getId(), result.id());
        assertEquals("FedEx", result.carrier());
    }

    @Test
    void givenNonExistingId_whenFindById_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.findById(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // findByTrackingCode
    // -------------------------------------------------------------------------

    @Test
    void givenExistingTrackingCode_whenFindByTrackingCode_thenReturnsDTO() {
        repository.add(buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED));

        ShipmentDTO result = service.findByTrackingCode("TRK-001AA");

        assertEquals("TRK-001AA", result.trackingCode());
    }

    @Test
    void givenNonExistingTrackingCode_whenFindByTrackingCode_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.findByTrackingCode("TRK-UNKNOWN"));
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenShipmentIsRemoved() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);

        service.delete(shipment.getId());

        assertNull(repository.findById(shipment.getId()));
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
    void givenValidRequest_whenSave_thenShipmentIsPersistedWithCreatedStatus() {
        ShipmentRequest request = new ShipmentRequest("FedEx", null);

        ShipmentDTO result = service.save(request);

        assertNotNull(result.id());
        assertNotNull(repository.findById(result.id()));
        assertEquals(ShipmentStatus.CREATED.name(), result.status());
    }

    @Test
    void givenValidRequest_whenSave_thenTrackingCodeIsGenerated() {
        ShipmentRequest request = new ShipmentRequest("FedEx", null);

        ShipmentDTO result = service.save(request);

        assertNotNull(result.trackingCode());
        assertTrue(result.trackingCode().startsWith("TRK-"));
    }

    @Test
    void givenNullRequest_whenSave_thenThrowsBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> service.save( null));
    }

    @Test
    void givenEmptyCarrier_whenSave_thenThrowsBadRequestException() {
        ShipmentRequest request = new ShipmentRequest("", null);

        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    // -------------------------------------------------------------------------
    // save (update)
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenSaveWithId_thenShipmentIsUpdated() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);
        ShipmentRequest request = new ShipmentRequest("DHL", null);

        ShipmentDTO result = service.save(shipment.getId(), request);

        assertEquals("DHL", result.carrier());
    }

    @Test
    void givenNonExistingId_whenSaveWithId_thenThrowsElementNotFoundException() {
        ShipmentRequest request = new ShipmentRequest("DHL", null);

        assertThrows(ElementNotFoundException.class,
                () -> service.save(UUID.randomUUID(), request));
    }

    @Test
    void givenNullRequest_whenSaveWithId_thenThrowsBadRequestException() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);

        assertThrows(BadRequestException.class,
                () -> service.save(shipment.getId(), null));
    }

    // -------------------------------------------------------------------------
    // changeStatus
    // -------------------------------------------------------------------------

    @Test
    void givenValidStatus_whenChangeStatus_thenStatusIsUpdated() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);
        ShipmentRequest request = new ShipmentRequest(null, "IN_TRANSIT");

        ShipmentDTO result = service.changeStatus(shipment.getId(), request);

        assertEquals("IN_TRANSIT", result.status());
    }

    @Test
    void givenValidStatus_whenChangeStatus_thenKafkaEventIsSent() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);
        ShipmentRequest request = new ShipmentRequest(null, "IN_TRANSIT");

        service.changeStatus(shipment.getId(), request);

        assertTrue(kafkaService.wasCalled());
        assertTrue(kafkaService.wasCalledWith(shipment.getId().toString()));
    }

    @Test
    void givenNullRequest_whenChangeStatus_thenThrowsBadRequestException() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);

        assertThrows(BadRequestException.class,
                () -> service.changeStatus(shipment.getId(), null));
    }

    @Test
    void givenNullStatus_whenChangeStatus_thenThrowsBadRequestException() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);
        ShipmentRequest request = new ShipmentRequest(null, null);

        assertThrows(BadRequestException.class,
                () -> service.changeStatus(shipment.getId(), request));
    }

    @Test
    void givenInvalidStatus_whenChangeStatus_thenThrowsBadRequestException() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);
        ShipmentRequest request = new ShipmentRequest(null, "INVALID_STATUS");

        assertThrows(BadRequestException.class,
                () -> service.changeStatus(shipment.getId(), request));
    }

    @Test
    void givenSameStatus_whenChangeStatus_thenThrowsBadRequestException() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);
        ShipmentRequest request = new ShipmentRequest(null, "CREATED");

        assertThrows(BadRequestException.class,
                () -> service.changeStatus(shipment.getId(), request));
    }

    @Test
    void givenNonExistingId_whenChangeStatus_thenThrowsElementNotFoundException() {
        ShipmentRequest request = new ShipmentRequest(null, "SHIPPED");

        assertThrows(ElementNotFoundException.class,
                () -> service.changeStatus(UUID.randomUUID(), request));
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Shipment buildShipment(String trackingCode, String carrier, ShipmentStatus status) {
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setTrackingCode(trackingCode);
        shipment.setCarrier(carrier);
        shipment.setStatus(status);
        shipment.setCreatedAt(Instant.now());
        shipment.setUpdatedAt(Instant.now());
        return shipment;
    }
}