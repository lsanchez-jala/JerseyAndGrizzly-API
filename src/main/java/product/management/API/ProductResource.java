package product.management.API;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import product.management.Application.IProductService;
import product.management.Domain.DTO.Product.ProductDTO;
import product.management.Domain.DTO.Product.ProductRequest;

import java.net.URI;
import java.util.UUID;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final IProductService service;

    @Inject
    public ProductResource(IProductService service) {
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

    @POST
    public Response create(ProductRequest request, @Context UriInfo uriInfo) {
        ProductDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @PATCH
    @Path("{id}")
    public Response update(@PathParam("id") UUID id, ProductRequest request) {
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
