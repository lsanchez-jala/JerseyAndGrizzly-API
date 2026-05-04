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
import product.management.API.ShipmentResource;
import product.management.Application.IShipmentService;
import Mocks.Services.FakeKafkaProducerService;
import product.management.Application.exception.GenericExceptionMapper;
import product.management.Application.impl.ShipmentService;
import product.management.Domain.DTO.Shipment.ShipmentRequest;
import product.management.Domain.Enums.ShipmentStatus;
import product.management.Domain.Models.Shipment;
import product.management.Infrastructure.Mappers.ShipmentMapper;
import Mocks.Repositories.FakeShipmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentResourceTest extends JerseyTest {

    private FakeShipmentRepository repository;
    private FakeKafkaProducerService kafkaService;

    @Override
    protected Application configure() {
        repository   = new FakeShipmentRepository();
        kafkaService = new FakeKafkaProducerService();

        IShipmentService shipmentService = new ShipmentService(
                repository,
                new ShipmentMapper(),
                kafkaService
        );

        ResourceConfig config = new ResourceConfig(ShipmentResource.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(shipmentService).to(IShipmentService.class);
            }
        });
        config.register(GenericExceptionMapper.class);

        return config;
    }

    // -------------------------------------------------------------------------
    // GET /shipments
    // -------------------------------------------------------------------------

    @Test
    void givenNoShipments_whenList_thenReturns200AndEmptyList() {
        Response response = target("/shipments").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        assertTrue(response.readEntity(List.class).isEmpty());
    }

    @Test
    void givenShipments_whenList_thenReturns200AndList() {
        repository.add(buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED));
        repository.add(buildShipment("TRK-002BB", "DHL", ShipmentStatus.CREATED));

        Response response = target("/shipments").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(2, response.readEntity(List.class).size());
    }

    // -------------------------------------------------------------------------
    // GET /shipments/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenGet_thenReturns200() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);

        Response response = target("/shipments/" + shipment.getId()).request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenNonExistingId_whenGet_thenReturns404() {
        Response response = target("/shipments/" + UUID.randomUUID()).request().get();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // GET /shipments/tracking/{trackingCode}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingTrackingCode_whenGetByTrackingCode_thenReturns200() {
        repository.add(buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED));

        Response response = target("/shipments/tracking/TRK-001AA").request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenNonExistingTrackingCode_whenGetByTrackingCode_thenReturns404() {
        Response response = target("/shipments/tracking/TRK-UNKNOWN").request().get();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // POST /shipments
    // -------------------------------------------------------------------------

    @Test
    void givenValidRequest_whenCreate_thenReturns201AndLocationHeader() {
        ShipmentRequest request = new ShipmentRequest("FedEx", null);

        Response response = target("/shipments")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        assertNotNull(response.getHeaderString("Location"));
        assertTrue(response.getHeaderString("Location").contains("/shipments/"));
    }

    @Test
    void givenValidRequest_whenCreate_thenShipmentIsPersisted() {
        ShipmentRequest request = new ShipmentRequest("FedEx", null);

        target("/shipments")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(1, repository.getStore().size());
    }

    @Test
    void givenValidRequest_whenCreate_thenTrackingCodeIsGenerated() {
        ShipmentRequest request = new ShipmentRequest("FedEx", null);

        Response response = target("/shipments")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        Shipment persisted = repository.getStore().values().iterator().next();
        assertNotNull(persisted.getTrackingCode());
        assertTrue(persisted.getTrackingCode().startsWith("TRK-"));
    }

    @Test
    void givenNullRequest_whenCreate_thenReturns400() {
        Response response = target("/shipments")
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    @Test
    void givenEmptyCarrier_whenCreate_thenReturns400() {
        ShipmentRequest request = new ShipmentRequest("", null);

        Response response = target("/shipments")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // PATCH /shipments/{id}
    // -------------------------------------------------------------------------

//    @Test
//    void givenExistingId_whenUpdate_thenReturns200() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//        ShipmentRequest request = new ShipmentRequest("DHL", null);
//
//        Response response = target("/shipments/" + shipment.getId())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(200, response.getStatus());
//    }
//
//    @Test
//    void givenExistingId_whenUpdate_thenShipmentIsUpdated() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//        ShipmentRequest request = new ShipmentRequest("DHL", null);
//
//        target("/shipments/" + shipment.getId())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals("DHL", repository.findById(shipment.getId()).getCarrier());
//    }
//
//    @Test
//    void givenNonExistingId_whenUpdate_thenReturns404() {
//        ShipmentRequest request = new ShipmentRequest("DHL", null);
//
//        Response response = target("/shipments/" + UUID.randomUUID())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(404, response.getStatus());
//    }
//
//    @Test
//    void givenNullRequestInUpdate_whenUpdate_thenReturns400() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//
//        Response response = target("/shipments/" + shipment.getId())
//                .request()
//                .method("PATCH", Entity.entity(null, MediaType.APPLICATION_JSON));
//
//        assertEquals(400, response.getStatus());
//    }

    // -------------------------------------------------------------------------
    // DELETE /shipments/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenReturns204() {
        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
        repository.add(shipment);

        Response response = target("/shipments/" + shipment.getId()).request().delete();

        assertEquals(204, response.getStatus());
        assertNull(repository.findById(shipment.getId()));
    }

    @Test
    void givenNonExistingId_whenDelete_thenReturns404() {
        Response response = target("/shipments/" + UUID.randomUUID()).request().delete();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // PATCH /shipments/{id}/status
    // -------------------------------------------------------------------------

//    @Test
//    void givenValidStatus_whenChangeStatus_thenReturns201AndLocationHeader() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//        ShipmentRequest request = new ShipmentRequest(null, "SHIPPED");
//
//        Response response = target("/shipments/" + shipment.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(201, response.getStatus());
//        assertNotNull(response.getHeaderString("Location"));
//    }
//
//    @Test
//    void givenValidStatus_whenChangeStatus_thenStatusIsUpdated() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//        ShipmentRequest request = new ShipmentRequest(null, "DELIVERED");
//
//        target("/shipments/" + shipment.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(ShipmentStatus.DELIVERED, repository.findById(shipment.getId()).getStatus());
//    }
//
//    @Test
//    void givenValidStatus_whenChangeStatus_thenKafkaEventIsSent() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//        ShipmentRequest request = new ShipmentRequest(null, "SHIPPED");
//        kafkaService.reset();
//
//        target("/shipments/" + shipment.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertTrue(kafkaService.wasCalled());
//        assertTrue(kafkaService.wasCalledWith(shipment.getId().toString()));
//    }
//
//    @Test
//    void givenInvalidStatus_whenChangeStatus_thenReturns400() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//        ShipmentRequest request = new ShipmentRequest(null, "INVALID_STATUS");
//
//        Response response = target("/shipments/" + shipment.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(400, response.getStatus());
//    }
//
//    @Test
//    void givenSameStatus_whenChangeStatus_thenReturns400() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//        ShipmentRequest request = new ShipmentRequest(null, "CREATED");
//
//        Response response = target("/shipments/" + shipment.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(400, response.getStatus());
//    }
//
//    @Test
//    void givenNullRequest_whenChangeStatus_thenReturns400() {
//        Shipment shipment = buildShipment("TRK-001AA", "FedEx", ShipmentStatus.CREATED);
//        repository.add(shipment);
//
//        Response response = target("/shipments/" + shipment.getId() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(null, MediaType.APPLICATION_JSON));
//
//        assertEquals(400, response.getStatus());
//    }
//
//    @Test
//    void givenNonExistingId_whenChangeStatus_thenReturns404() {
//        ShipmentRequest request = new ShipmentRequest(null, "SHIPPED");
//
//        Response response = target("/shipments/" + UUID.randomUUID() + "/status")
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(404, response.getStatus());
//    }

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