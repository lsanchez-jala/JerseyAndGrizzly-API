package Mocks.Repositories;

import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Domain.Models.Product;
import product.management.Infrastructure.Repositories.IProductRepository;

import java.util.*;

public class FakeProductRepository implements IProductRepository {

    private final Map<UUID, Product> store = new HashMap<>();

    // --- helpers for test setup ---

    public void add(Product product) {
        store.put(product.getId(), product);
    }

    public Map<UUID, Product> getStore() {
        return Collections.unmodifiableMap(store);
    }

    // --- interface implementation ---

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Product findById(UUID id) {
        return store.get(id); // null if not found — mirrors real repo behavior
    }

    @Override
    public Product findBySku(String sku) {
        return store.values().stream()
                .filter(p -> sku.equals(p.getSku()))
                .findFirst()
                .orElse(null); // null if not found — mirrors real repo behavior
    }

    @Override
    public Product save(ProductRequest request) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName(request.name());
        product.setSku(request.sku());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setStock(request.stock());
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public Product save(UUID id, Product entity) {
        if (!store.containsKey(id)) {
            throw new RuntimeException("No product found with id: " + id);
        }
        store.put(id, entity);
        return entity;
    }

    @Override
    public void delete(UUID id) {
        if (!store.containsKey(id)) {
            throw new RuntimeException("No product found with id: " + id);
        }
        store.remove(id);
    }
}
