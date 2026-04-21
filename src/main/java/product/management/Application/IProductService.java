package product.management.Application;

import product.management.Domain.DTO.Product.ProductDTO;
import product.management.Domain.DTO.Product.ProductRequest;

import java.util.List;
import java.util.UUID;

public interface IProductService {
    List<ProductDTO> findAll();
    ProductDTO findById(UUID id);
    ProductDTO findBySku(String sku);
    void delete(UUID id);
    ProductDTO save(ProductRequest request);
    ProductDTO save(UUID id, ProductRequest request);
}
