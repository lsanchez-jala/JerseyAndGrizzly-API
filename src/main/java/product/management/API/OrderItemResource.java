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
import product.management.Application.IOrderItemService;
import product.management.Domain.DTO.OrderItem.OrderItemDTO;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;

import java.net.URI;
import java.util.UUID;
@Path("/order-items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Order Items", description = "Order item management operations")
public class OrderItemResource {

    private final IOrderItemService service;
    private final Logger logger = LoggerFactory.getLogger(OrderItemResource.class);

    @Inject
    public OrderItemResource(IOrderItemService service) {
        this.service = service;
    }

    @GET
    @Operation(
            summary = "List all order items",
            description = "Returns a list of all order items",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of order items retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderItemDTO.class)))
                    )
            }
    )
    public Response list(@Context UriInfo uriInfo) {
        URI location = uriInfo.getRequestUri();
        logger.info("GET {}", location);
        return Response.ok(service.findAll()).build();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Get order item by ID",
            description = "Returns a single order item identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order item found",
                            content = @Content(schema = @Schema(implementation = OrderItemDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Order item not found")
            }
    )
    public Response get(
            @Parameter(description = "UUID of the order item to retrieve", required = true)
            @PathParam("id") UUID id,
            @Context UriInfo uriInfo
    ) {
        URI location = uriInfo.getRequestUri();
        logger.info("GET {}", location);
        return Response.ok(service.findById(id)).build();
    }

    @GET
    @Path("order/{orderId}")
    @Operation(
            summary = "List order items by order ID",
            description = "Returns all order items belonging to a specific order",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of order items retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderItemDTO.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            }
    )
    public Response listByOrderId(
            @Parameter(description = "UUID of the order to retrieve items for", required = true)
            @PathParam("orderId") UUID orderId,
            @Context UriInfo uriInfo
    ) {
        URI location = uriInfo.getRequestUri();
        logger.info("GET {}", location);
        return Response.ok(service.findByOrderId(orderId)).build();
    }

    @GET
    @Path("product/{productId}")
    @Operation(
            summary = "List order items by product ID",
            description = "Returns all order items associated with a specific product",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of order items retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderItemDTO.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public Response listByProductId(
            @Parameter(description = "UUID of the product to retrieve order items for", required = true)
            @PathParam("productId") UUID productId,
            @Context UriInfo uriInfo
    ) {
        URI location = uriInfo.getRequestUri();
        logger.info("GET {}", location);
        return Response.ok(service.findByProductId(productId)).build();
    }

    @POST
    @Operation(
            summary = "Create a new order item",
            description = "Creates a new order item and returns the created resource",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Order item created successfully",
                            content = @Content(schema = @Schema(implementation = OrderItemDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response create(
            @RequestBody(
                    description = "Order item data to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderItemRequest.class))
            )
            OrderItemRequest request,
            @Context UriInfo uriInfo
    ) {
        OrderItemDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        logger.info("POST {}",  uriInfo.getRequestUri());
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("{id}")
    @Operation(
            summary = "Update order item by ID",
            description = "Updates an existing order item identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order item updated successfully",
                            content = @Content(schema = @Schema(implementation = OrderItemDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Order item not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response update(
            @Parameter(description = "UUID of the order item to update", required = true)
            @PathParam("id") UUID id,
            @RequestBody(
                    description = "Updated order item data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderItemRequest.class))
            )
            OrderItemRequest request,
            @Context UriInfo uriInfo
    ) {
        URI location = uriInfo.getRequestUri();
        logger.info("PUT {}", location);
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete order item by ID",
            description = "Deletes an existing order item identified by its UUID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Order item deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Order item not found")
            }
    )
    public Response delete(
            @Parameter(description = "UUID of the order item to delete", required = true)
            @PathParam("id") UUID id,
            @Context UriInfo uriInfo
    ) {
        service.delete(id);
        logger.info("DELETE {}", uriInfo.getRequestUri());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/order/{orderId}")
    @Operation(
            summary = "Delete all order items by order ID",
            description = "Deletes all order items associated with a specific order",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Order items deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            }
    )
    public Response deleteByOrderId(
            @Parameter(description = "UUID of the order whose items should be deleted", required = true)
            @PathParam("orderId") UUID orderId,
            @Context UriInfo uriInfo
    ) {
        service.deleteByOrderId(orderId);
        logger.info("DELETE {}", uriInfo.getRequestUri());
        return Response.noContent().build();
    }
}