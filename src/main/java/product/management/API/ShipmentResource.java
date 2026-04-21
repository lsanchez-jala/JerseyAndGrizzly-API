package product.management.API;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import product.management.Application.IShipmentService;
import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;

import java.net.URI;
import java.util.UUID;

@Path("/shipments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShipmentResource {

    private final IShipmentService service;

    @Inject
    public ShipmentResource(IShipmentService service) {
        this.service = service;
    }

    @GET
    public Response list() {
        return Response.ok(service.findAll()).build();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(service.findById(id)).build();
    }

    @GET
    @Path("tracking/{trackingCode}")
    public Response getByTrackingCode(@PathParam("trackingCode") String trackingCode) {
        return Response.ok(service.findByTrackingCode(trackingCode)).build();
    }

    @POST
    public Response create(ShipmentRequest request, @Context UriInfo uriInfo) {
        ShipmentDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @PATCH
    @Path("{id}")
    public Response update(@PathParam("id") UUID id, ShipmentRequest request) {
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}/status")
    public Response changeStatus(@PathParam("id") UUID id, ShipmentRequest request, @Context UriInfo uriInfo) {
        ShipmentDTO created = service.changeStatus(id, request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }
}