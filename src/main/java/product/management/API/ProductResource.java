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
import product.management.Application.IProductService;
import product.management.Domain.DTO.Product.ProductDTO;
import product.management.Domain.DTO.Product.ProductRequest;

import java.net.URI;
import java.util.UUID;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Products", description = "Product management operations")
public class ProductResource {

    private final IProductService service;
    private Logger logger = LoggerFactory.getLogger(ProductResource.class);

    @Inject
    public ProductResource(IProductService service) {
        this.service = service;
    }

    @GET
    @Operation(
            summary = "List all products",
            description = "Returns a list of all available products",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of products retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class)))
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
            summary = "Get product by ID",
            description = "Returns a single product identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Product found",
                            content = @Content(schema = @Schema(implementation = ProductDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public Response get(
            @Parameter(description = "UUID of the product to retrieve", required = true)
            @PathParam("id") UUID id,
            @Context UriInfo uriInfo
    ) {
        logger.info("GET {}", uriInfo.getRequestUri());
        return Response.ok(service.findById(id)).build();
    }

    @POST
    @Operation(
            summary = "Create a new product",
            description = "Creates a new product and returns the created resource",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Product created successfully",
                            content = @Content(schema = @Schema(implementation = ProductDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response create(
            @RequestBody(
                    description = "Product data to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductRequest.class))
            )
            ProductRequest request,
            @Context UriInfo uriInfo
    ) {
        logger.info("POST {}", uriInfo.getRequestUri());
        ProductDTO created = service.save(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }

    @PATCH
    @Path("{id}")
    @Operation(
            summary = "Partially update product by ID",
            description = "Updates one or more fields of an existing product identified by its UUID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Product updated successfully",
                            content = @Content(schema = @Schema(implementation = ProductDTO.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Product not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid request payload")
            }
    )
    public Response update(
            @Parameter(description = "UUID of the product to update", required = true)
            @PathParam("id") UUID id,
            @RequestBody(
                    description = "Updated product data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductRequest.class))
            )
            ProductRequest request,
            @Context UriInfo uriInfo
    ) {
        logger.info("PATCH {}", uriInfo.getRequestUri());
        return Response.ok(service.save(id, request)).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete product by ID",
            description = "Deletes an existing product identified by its UUID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public Response delete(
            @Parameter(description = "UUID of the product to delete", required = true)
            @PathParam("id") UUID id,
            @Context UriInfo uriInfo
    ) {
        service.delete(id);
        logger.info("DELETE {}", uriInfo.getRequestUri());
        return Response.noContent().build();
    }
}