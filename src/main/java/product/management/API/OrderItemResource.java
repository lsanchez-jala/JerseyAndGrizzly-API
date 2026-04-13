package product.management.API;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import product.management.Application.OrderItemService;
import product.management.Domain.DTO.OrderItem.OrderItemDTO;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;

import java.net.URI;
import java.util.UUID;

@Path("/order-items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderItemResource {

    private final OrderItemService service;

    @Inject
    public OrderItemResource(OrderItemService service) {
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
    @Path("order/{orderId}")
    public Response listByOrderId(@PathParam("orderId") UUID orderId) {
        return Response.ok(service.findByOrderId(orderId)).build();
    }

    @GET
    @Path("product/{productId}")
    public Response listByProductId(@PathParam("productId") UUID productId) {
        return Response.ok(service.findByProductId(productId)).build();
    }

    @POST
    public Response create(OrderItemRequest request, @Context UriInfo uriInfo) {
        OrderItemDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") UUID id, OrderItemRequest request) {
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
