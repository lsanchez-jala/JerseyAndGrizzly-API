package product.management.Infrastructure.Repositories;

import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Domain.Models.Product;

import java.util.List;
import java.util.UUID;

public interface IProductRepository {
    List<Product> findAll();
    Product findById(UUID id);
    Product findBySku(String sku);
    Product save(ProductRequest request);
    void delete(UUID id);
    Product save(UUID id, Product request);
}
