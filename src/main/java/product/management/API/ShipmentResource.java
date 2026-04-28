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
import product.management.Application.IShipmentService;
import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;

import java.net.URI;
import java.util.UUID;

@Path("/shipments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Shipments", description = "Shipment management operations")
public class ShipmentResource {

    private final IShipmentService service;

    @Inject
    public ShipmentResource(IShipmentService service) {
        this.service = service;
    }

    @GET
    @Operation(
            summary = "List all shipments",
            description = "Returns a list of all shipments",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of shipments retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ShipmentDTO.class)))
                    )
            }
    )
    public Response list() {
        return Response.ok(service.findAll()).build();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Get shipment by ID",
            description = "Returns a single shipment identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Shipment found",
                            content = @Content(schema = @Schema(implementation = ShipmentDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Shipment not found")
            }
    )
    public Response get(
            @Parameter(description = "UUID of the shipment to retrieve", required = true)
            @PathParam("id") UUID id
    ) {
        return Response.ok(service.findById(id)).build();
    }

    @GET
    @Path("tracking/{trackingCode}")
    @Operation(
            summary = "Get shipment by tracking code",
            description = "Returns a single shipment identified by its tracking code",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Shipment found",
                            content = @Content(schema = @Schema(implementation = ShipmentDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Shipment not found")
            }
    )
    public Response getByTrackingCode(
            @Parameter(description = "Tracking code of the shipment to retrieve", required = true)
            @PathParam("trackingCode") String trackingCode
    ) {
        return Response.ok(service.findByTrackingCode(trackingCode)).build();
    }

    @POST
    @Operation(
            summary = "Create a new shipment",
            description = "Creates a new shipment and returns the created resource",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Shipment created successfully",
                            content = @Content(schema = @Schema(implementation = ShipmentDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response create(
            @RequestBody(
                    description = "Shipment data to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ShipmentRequest.class))
            )
            ShipmentRequest request,
            @Context UriInfo uriInfo
    ) {
        ShipmentDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @PATCH
    @Path("{id}")
    @Operation(
            summary = "Partially update shipment by ID",
            description = "Updates one or more fields of an existing shipment identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Shipment updated successfully",
                            content = @Content(schema = @Schema(implementation = ShipmentDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Shipment not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response update(
            @Parameter(description = "UUID of the shipment to update", required = true)
            @PathParam("id") UUID id,
            @RequestBody(
                    description = "Updated shipment data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ShipmentRequest.class))
            )
            ShipmentRequest request
    ) {
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete shipment by ID",
            description = "Deletes an existing shipment identified by its UUID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Shipment deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Shipment not found")
            }
    )
    public Response delete(
            @Parameter(description = "UUID of the shipment to delete", required = true)
            @PathParam("id") UUID id
    ) {
        service.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}/status")
    @Operation(
            summary = "Change shipment status",
            description = "Updates the status of an existing shipment identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Shipment status changed successfully",
                            content = @Content(schema = @Schema(implementation = ShipmentDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Shipment not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid status transition")
            }
    )
    public Response changeStatus(
            @Parameter(description = "UUID of the shipment to update status for", required = true)
            @PathParam("id") UUID id,
            @RequestBody(
                    description = "New status data for the shipment",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ShipmentRequest.class))
            )
            ShipmentRequest request,
            @Context UriInfo uriInfo
    ) {
        ShipmentDTO created = service.changeStatus(id, request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }
}