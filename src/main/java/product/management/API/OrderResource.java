package product.management.API;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import product.management.Application.OrderService;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderRequest;

import java.net.URI;
import java.util.UUID;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderService service;

    @Inject
    public OrderResource(OrderService service) {
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
    @Path("customer/{customerId}")
    public Response listByCustomer(@PathParam("customerId") UUID customerId) {
        return Response.ok(service.findByCustomerId(customerId)).build();
    }

    @POST
    public Response create(OrderRequest request, @Context UriInfo uriInfo) {
        OrderDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") UUID id, OrderRequest request) {
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/status")
    public Response changeStatus(@PathParam("id") UUID id, OrderRequest request, @Context UriInfo uriInfo) {
        OrderDTO created = service.changeStatus(id, request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }
}