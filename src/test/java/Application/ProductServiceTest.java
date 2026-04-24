package Application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.management.Application.exception.BadRequestException;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Application.impl.ProductService;
import product.management.Domain.DTO.Product.ProductDTO;
import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Domain.Models.Product;
import product.management.Infrastructure.Mappers.ProductMapper;
import product.management.Infrastructure.Repositories.mocks.FakeProductRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {

    private FakeProductRepository repository;
    private ProductService service;

    @BeforeEach
    void setUp() {
        repository = new FakeProductRepository();
        service = new ProductService(repository, new ProductMapper());
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void givenProducts_whenFindAll_thenReturnsAllMapped() {
        repository.add(buildProduct("Phone", "SKU-001"));
        repository.add(buildProduct("Tablet", "SKU-002"));

        List<ProductDTO> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void givenNoProducts_whenFindAll_thenReturnsEmptyList() {
        assertTrue(service.findAll().isEmpty());
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenFindById_thenReturnsDTO() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);

        ProductDTO result = service.findById(product.getId());

        assertEquals(product.getId(), result.id());
        assertEquals("Phone", result.name());
    }

    @Test
    void givenNonExistingId_whenFindById_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.findById(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // findBySku
    // -------------------------------------------------------------------------

    @Test
    void givenExistingSku_whenFindBySku_thenReturnsDTO() {
        repository.add(buildProduct("Phone", "SKU-001"));

        ProductDTO result = service.findBySku("SKU-001");

        assertEquals("SKU-001", result.sku());
    }

    @Test
    void givenNonExistingSku_whenFindBySku_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.findBySku("SKU-999"));
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void givenExistingId_whenDelete_thenProductIsRemoved() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);

        service.delete(product.getId());

        assertNull(repository.findById(product.getId()));
    }

    @Test
    void givenNonExistingId_whenDelete_thenThrowsElementNotFoundException() {
        assertThrows(ElementNotFoundException.class,
                () -> service.delete(UUID.randomUUID()));
    }

    // -------------------------------------------------------------------------
    // save (create)
    // -------------------------------------------------------------------------

    @Test
    void givenValidRequest_whenSave_thenProductIsPersisted() {
        ProductRequest request = new ProductRequest("Phone", "SKU-001", 999.54F, 10, "Electronics");

        ProductDTO result = service.save(request);

        assertNotNull(result.id());
        assertNotNull(repository.findBySku("SKU-001"));
        assertEquals("Phone", result.name());
    }

    @Test
    void givenNullRequest_whenSave_thenThrowsBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> service.save( null));
    }

    @Test
    void givenNullName_whenSave_thenThrowsBadRequestException() {
        ProductRequest request = new ProductRequest(null, "SKU-001", 999.0F,10, "Electronics");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenBlankName_whenSave_thenThrowsBadRequestException() {
        ProductRequest request = new ProductRequest("", "SKU-001", 999.0F,10, "Electronics");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenNullSku_whenSave_thenThrowsBadRequestException() {
        ProductRequest request = new ProductRequest("Phone", null, 999.0F,10, "Electronics");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenNullCategory_whenSave_thenThrowsBadRequestException() {
        ProductRequest request = new ProductRequest("Phone", "SKU-001", null, 999, "Electronics");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenNullPrice_whenSave_thenThrowsBadRequestException() {
        ProductRequest request = new ProductRequest("Phone", "SKU-001", 999.0F, null, "Electronics");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenNegativePrice_whenSave_thenThrowsBadRequestException() {
        ProductRequest request = new ProductRequest("Phone", "SKU-001", -1.0F, 10, "Electronics");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenZeroStock_whenSave_thenThrowsBadRequestException() {
        ProductRequest request = new ProductRequest("Phone", "SKU-001", 999.0F,0, "Electronics");
        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    @Test
    void givenDuplicateSku_whenSave_thenThrowsBadRequestException() {
        repository.add(buildProduct("Phone", "SKU-001"));
        ProductRequest request = new ProductRequest("Other", "SKU-001", 999.0F,10, "Electronics");

        assertThrows(BadRequestException.class, () -> service.save(request));
    }

    // -------------------------------------------------------------------------
    // save (update)
    // -------------------------------------------------------------------------

    @Test
    void givenValidUpdate_whenSaveWithId_thenProductIsUpdated() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);
        ProductRequest request = new ProductRequest("Tablet", null, null, null, null);

        ProductDTO result = service.save(product.getId(), request);

        assertEquals("Tablet", result.name());
    }

    @Test
    void givenNonExistingId_whenSaveWithId_thenThrowsElementNotFoundException() {
        ProductRequest request = new ProductRequest("Tablet", null, null, null, null);

        assertThrows(ElementNotFoundException.class,
                () -> service.save(UUID.randomUUID(), request));
    }

    @Test
    void givenBlankNameInUpdate_whenSaveWithId_thenThrowsBadRequestException() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);
        ProductRequest request = new ProductRequest("   ", null, null, null, null);

        assertThrows(BadRequestException.class,
                () -> service.save(product.getId(), request));
    }

    @Test
    void givenDuplicateSkuInUpdate_whenSaveWithId_thenThrowsBadRequestException() {
        repository.add(buildProduct("Phone", "SKU-001"));
        Product other = buildProduct("Tablet", "SKU-002");
        repository.add(other);
        ProductRequest request = new ProductRequest(null, "SKU-001", null, null, null);

        assertThrows(BadRequestException.class,
                () -> service.save(other.getId(), request));
    }

    @Test
    void givenNegativePriceInUpdate_whenSaveWithId_thenThrowsBadRequestException() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);
        ProductRequest request = new ProductRequest(null, null, -5.0F, null, null);

        assertThrows(BadRequestException.class,
                () -> service.save(product.getId(), request));
    }

    @Test
    void givenZeroStockInUpdate_whenSaveWithId_thenThrowsBadRequestException() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);
        ProductRequest request = new ProductRequest(null, null, null, 0, null);

        assertThrows(BadRequestException.class,
                () -> service.save(product.getId(), request));
    }

    @Test
    void givenBlankCategoryInUpdate_whenSaveWithId_thenThrowsBadRequestException() {
        Product product = buildProduct("Phone", "SKU-001");
        repository.add(product);
        ProductRequest request = new ProductRequest(null, null, null, null, "  ");

        assertThrows(BadRequestException.class,
                () -> service.save(product.getId(), request));
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
