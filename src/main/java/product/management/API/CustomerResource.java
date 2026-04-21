package product.management.API;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import product.management.Application.ICustomerService;
import product.management.Domain.DTO.Customer.CustomerDTO;
import product.management.Domain.DTO.Customer.CustomerRequest;

import java.net.URI;
import java.util.UUID;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private final ICustomerService service;

    @Inject
    public CustomerResource(ICustomerService service) {
        this.service = service;
    }

    @GET
    public Response list() {
        var result = service.findAll();
        return Response.ok(result).build();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") UUID id) {
        return Response.ok(service.findById(id)).build();
    }

    @GET
    @Path("email/{email}")
    public Response getByEmail(@PathParam("email") String email) {
        return Response.ok(service.findByEmail(email)).build();
    }

    @POST
    public Response create(CustomerRequest request, @Context UriInfo uriInfo) {
        CustomerDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") UUID id, CustomerRequest request) {
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
