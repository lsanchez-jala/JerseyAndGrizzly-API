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
import jakarta.validation.constraints.Null;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import product.management.Application.IOrderService;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderCreateRequest;
import product.management.Domain.DTO.Order.OrderStatusRequest;

import java.net.URI;
import java.util.UUID;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Orders", description = "Order management operations")
public class OrderResource {

    private final IOrderService service;
    private Logger logger = LoggerFactory.getLogger(OrderResource.class);

    @Inject
    public OrderResource(IOrderService service) {
        this.service = service;
    }

    @GET
    @Operation(
            summary = "List all orders",
            description = "Returns a list of all orders",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of orders retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDTO.class)))
                    )
            }
    )
    public Response list(@Context UriInfo uriInfo) {
        logger.info("GET {}", uriInfo.getRequestUri());
        return Response.ok(service.findAll()).build();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Get order by ID",
            description = "Returns a single order identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order found",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            }
    )
    public Response get(
            @Parameter(description = "UUID of the order to retrieve", required = true)
            @PathParam("id") UUID id,
            @Context UriInfo uriInfo
    ) {
        logger.info("GET {}", uriInfo.getRequestUri());
        return Response.ok(service.findById(id)).build();
    }

    @GET
    @Path("customer/{customerId}")
    @Operation(
            summary = "List orders by customer ID",
            description = "Returns all orders belonging to a specific customer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of orders retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDTO.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    public Response listByCustomer(
            @Parameter(description = "UUID of the customer to retrieve orders for", required = true)
            @PathParam("customerId") UUID customerId,
            @Context UriInfo uriInfo
    ) {
        logger.info("GET {}", uriInfo.getRequestUri());
        return Response.ok(service.findByCustomerId(customerId)).build();
    }

    @GET
    @Path("shipment/{shipmentId}")
    @Operation(
            summary = "List orders by shipment ID",
            description = "Returns all orders associated with a specific shipment",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of orders retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDTO.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "Shipment not found")
            }
    )
    public Response listByShipment(
            @Parameter(description = "UUID of the shipment to retrieve orders for", required = true)
            @PathParam("shipmentId") UUID shipmentId,
            @Context UriInfo uriInfo
    ) {
        logger.info("GET {}", uriInfo.getRequestUri());
        return Response.ok(service.findByShipmentId(shipmentId)).build();
    }

    @POST
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order and returns the created resource",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Order created successfully",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response create(
            @RequestBody(
                    description = "Order data to create",
                    required = false,
                    content = @Content(schema = @Schema(implementation = OrderCreateRequest.class))
            )
            OrderCreateRequest request,
            @Context UriInfo uriInfo
    ) {
        OrderDTO created = service.save(request);
        logger.info("POST {}", uriInfo.getRequestUri());
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @PATCH
    @Path("{id}")
    @Operation(
            summary = "Partially update order by ID",
            description = "Updates one or more fields of an existing order identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order updated successfully",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Order not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response update(
            @Parameter(description = "UUID of the order to update", required = true)
            @PathParam("id") UUID id,
            @RequestBody(
                    description = "Updated order data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderCreateRequest.class))
            )
            OrderCreateRequest request,
            @Context UriInfo uriInfo
    ) {
        logger.info("PATCH {}", uriInfo.getRequestUri());
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete order by ID",
            description = "Deletes an existing order identified by its UUID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            }
    )
    public Response delete(
            @Parameter(description = "UUID of the order to delete", required = true)
            @PathParam("id") UUID id,
            @Context UriInfo uriInfo
    ) {
        logger.info("DELETE {}", uriInfo.getRequestUri());
        service.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}/status")
    @Operation(
            summary = "Change order status",
            description = "Updates the status of an existing order identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Order status changed successfully",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Order not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid status transition")
            }
    )
    public Response changeStatus(
            @Parameter(description = "UUID of the order to update status for", required = true)
            @PathParam("id") UUID id,
            @RequestBody(
                    description = "New status data for the order",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderStatusRequest.class))
            )
            OrderStatusRequest request,
            @Context UriInfo uriInfo
    ) {
        logger.info("PATCH {}", uriInfo.getRequestUri());
        OrderDTO created = service.changeStatus(id, request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }
}