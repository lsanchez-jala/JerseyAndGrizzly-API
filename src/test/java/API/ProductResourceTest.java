package API;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import product.management.API.ProductResource;
import product.management.Application.IProductService;
import product.management.Application.exception.GenericExceptionMapper;
import product.management.Application.impl.ProductService;
import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Domain.Models.Product;
import product.management.Infrastructure.Mappers.ProductMapper;
import product.management.Infrastructure.Repositories.mocks.FakeProductRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductResourceTest extends JerseyTest {

    private FakeProductRepository repository;
    private IProductService service;

    @Override
    protected Application configure() {
        repository = new FakeProductRepository();
        service = new ProductService(repository, new ProductMapper());

        ResourceConfig config = new ResourceConfig(ProductResource.class);
        config.register(JacksonFeature.class); // enable JSON serialization
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(service).to(IProductService.class);
            }
        });

        // register your exception mappers so 404/400 are returned instead of 500
        config.register(GenericExceptionMapper.class);
        return config;
    }

    // -------------------------------------------------------------------------
    // GET /products
    // -------------------------------------------------------------------------

    @Test
    void givenNoProducts_whenList_thenReturns200AndEmptyList() {
        Response response = target("/products").request().get();

        assertEquals(200, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        List result = response.readEntity(List.class);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenProducts_whenList_thenReturns200AndList() {
        repository.add(buildProduct("Phone", "SKU-001"));
        repository.add(buildProduct("Tablet", "SKU-002"));

        Response response = target("/products").request().get();

        assertEquals(200, response.getStatus());
        List result = response.readEntity(List.class);
        assertEquals(2, result.size());
    }

    // -------------------------------------------------------------------------
    // GET /products/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenGet_thenReturns200() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);

        Response response = target("/products/" + product.getId()).request().get();

        assertEquals(200, response.getStatus());
    }

    @Test
    void givenNonExistingId_whenGet_thenReturns404() {
        Response response = target("/products/" + UUID.randomUUID()).request().get();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // POST /products
    // -------------------------------------------------------------------------

    @Test
    void givenValidRequest_whenCreate_thenReturns201AndLocationHeader() {
        ProductRequest request = new ProductRequest("Phone", "SKU-001", 999.0F, 10,"Electronics");

        Response response = target("/products")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        assertNotNull(response.getHeaderString("Location"));
        assertTrue(response.getHeaderString("Location").contains("/products/"));
    }

    @Test
    void givenValidRequest_whenCreate_thenProductIsPersisted() {
        ProductRequest request = new ProductRequest("Phone", "SKU-001", 999.0F, 10,"Electronics");

        Response response = target("/products")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(201, response.getStatus());
        assertNotNull(repository.findBySku("SKU-001")); // verify actually persisted
    }

    @Test
    void givenNullName_whenCreate_thenReturns400() {
        ProductRequest request = new ProductRequest(null, "SKU-001", 999.0F, 10,"Electronics");

        Response response = target("/products")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    @Test
    void givenNegativePrice_whenCreate_thenReturns400() {
        ProductRequest request = new ProductRequest("Phone", "SKU-001", -999.0F, 10,"Electronics");

        Response response = target("/products")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    @Test
    void givenDuplicateSku_whenCreate_thenReturns400() {
        repository.add(buildProduct("Phone", "SKU-001"));
        ProductRequest request = new ProductRequest("Other", "SKU-001", 999.0F, 10,"Electronics");

        Response response = target("/products")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertEquals(400, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // PATCH /products/{id}
    // -------------------------------------------------------------------------

//    @Test
//    void givenExistingId_whenUpdate_thenReturns200() {
//        Product product = buildProduct("Phone", "SKU-001");
//        repository.add(product);
//        ProductRequest request = new ProductRequest("Tablet", null, null, null, null);
//
//        Response response = target("/products/" + product.getId())
//                .request()
//                .method("PATCH",Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(200, response.getStatus());
//    }

//    @Test
//    void givenNonExistingId_whenUpdate_thenReturns404() {
//        ProductRequest request = new ProductRequest("Tablet", null, null, null, null);
//
//        Response response = target("/products/" + UUID.randomUUID())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(404, response.getStatus());
//    }

//    @Test
//    void givenBlankNameInUpdate_whenUpdate_thenReturns400() {
//        Product product = buildProduct("Phone", "SKU-001");
//        repository.add(product);
//        ProductRequest request = new ProductRequest("   ", null, null, null, null);
//
//        Response response = target("/products/" + product.getId())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(400, response.getStatus());
//    }

//    @Test
//    void givenDuplicateSkuInUpdate_whenUpdate_thenReturns400() {
//        repository.add(buildProduct("Phone", "SKU-001"));
//        Product other = buildProduct("Tablet", "SKU-002");
//        repository.add(other);
//        ProductRequest request = new ProductRequest(null, "SKU-001", null, null, null);
//
//        Response response = target("/products/" + other.getId())
//                .request()
//                .method("PATCH", Entity.entity(request, MediaType.APPLICATION_JSON));
//
//        assertEquals(400, response.getStatus());
//    }

    // -------------------------------------------------------------------------
    // DELETE /products/{id}
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenReturns204() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);

        Response response = target("/products/" + product.getId())
                .request()
                .delete();

        assertEquals(204, response.getStatus());
        assertNull(repository.findById(product.getId())); // verify actually deleted
    }

    @Test
    void givenNonExistingId_whenDelete_thenReturns404() {
        Response response = target("/products/" + UUID.randomUUID())
                .request()
                .delete();

        assertEquals(404, response.getStatus());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Product buildProduct(String name, String sku) {
        Product p = new Product();
        p.setId(UUID.randomUUID());
        p.setName(name);
        p.setSku(sku);
        p.setCategory("Electronics");
        p.setPrice(100.0F);
        p.setStock(5);
        return p;
    }
}