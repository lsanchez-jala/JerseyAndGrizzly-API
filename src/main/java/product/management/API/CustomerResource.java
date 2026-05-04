package product.management.API;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import product.management.Application.ICustomerService;
import product.management.Domain.DTO.Customer.CustomerDTO;
import product.management.Domain.DTO.Customer.CustomerRequest;

import java.net.URI;
import java.util.UUID;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customers", description = "Customer management operations")
public class CustomerResource {

    private final ICustomerService service;
    private final Logger logger = LoggerFactory.getLogger(CustomerResource.class);

    @Inject
    public CustomerResource(ICustomerService service) {
        this.service = service;
    }

    @GET
    @Operation(
            summary = "List all customers",
            description = "Returns a list of all registered customers",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of customers retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerDTO.class)))
                    )
            }
    )
    public Response list(@Context UriInfo uriInfo) {
        var result = service.findAll();
        URI location = uriInfo.getRequestUri();
        logger.info("GET {}", location);
        return Response.ok(result).build();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Get customer by ID",
            description = "Returns a single customer identified by their UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer found",
                            content = @Content(schema = @Schema(implementation = CustomerDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    public Response get(
            @Parameter(description = "UUID of the customer to retrieve", required = true)
            @PathParam("id") UUID id,
            @Context UriInfo uriInfo
    ) {
        URI location = uriInfo.getRequestUri();
        logger.info("GET {}", location);
        return Response.ok(service.findById(id)).build();
    }

    @GET
    @Path("email/{email}")
    @Operation(
            summary = "Get customer by email",
            description = "Returns a single customer identified by their email address",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer found",
                            content = @Content(schema = @Schema(implementation = CustomerDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    public Response getByEmail(
            @Parameter(description = "Email address of the customer to retrieve", required = true)
            @PathParam("email") String email,
            @Context UriInfo uriInfo
    ) {
        URI location = uriInfo.getRequestUri();
        logger.info("GET {}", location);
        return Response.ok(service.findByEmail(email)).build();
    }

    @POST
    @Operation(
            summary = "Create a new customer",
            description = "Creates a new customer and returns the created resource",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Customer created successfully",
                            content = @Content(schema = @Schema(implementation = CustomerDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response create(
            @RequestBody(
                    description = "Customer data to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CustomerRequest.class))
            )
            CustomerRequest request,
            @Context UriInfo uriInfo
    ) {
        CustomerDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        logger.info("POST {}",  uriInfo.getRequestUri());
        logger.info("Created customer with id {}", created.id());
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("{id}")
    @Operation(
            summary = "Update customer by ID",
            description = "Updates an existing customer identified by their UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer updated successfully",
                            content = @Content(schema = @Schema(implementation = CustomerDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Customer not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response update(
            @Parameter(description = "UUID of the customer to update", required = true)
            @PathParam("id") UUID id,
            @RequestBody(
                    description = "Updated customer data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CustomerRequest.class))
            )
            CustomerRequest request,
            @Context UriInfo uriInfo
    ) {
        URI location = uriInfo.getRequestUri();
        logger.info("PUT {}", location);
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete customer by ID",
            description = "Deletes an existing customer identified by their UUID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    public Response delete(
            @Parameter(description = "UUID of the customer to delete", required = true)
            @PathParam("id") UUID id,
            @Context UriInfo uriInfo
    ) {
        URI location = uriInfo.getRequestUri();
        logger.info("DELETE {}", location);
        service.delete(id);
        logger.info("Deleted customer with id {}", id);
        return Response.noContent().build();
    }
}