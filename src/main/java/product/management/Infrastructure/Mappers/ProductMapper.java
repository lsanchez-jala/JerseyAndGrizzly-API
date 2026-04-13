package product.management.Infrastructure.Mappers;

import jakarta.inject.Singleton;
import product.management.Domain.DTO.Product.ProductDTO;
import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Domain.Models.Product;

@Singleton
public class ProductMapper {

    public ProductDTO toDto(Product entity) {
        return new ProductDTO(
                entity.getId(),
                entity.getName(),
                entity.getSku(),
                entity.getPrice(),
                entity.getStock(),
                entity.getCategory()
        );
    }

    public void toEntity(ProductRequest request, Product entity) {
        if (request.name() != null)     entity.setName(request.name());
        if (request.sku() != null)      entity.setSku(request.sku());
        if (request.price() != null)    entity.setPrice(request.price());
        if (request.stock() != null)    entity.setStock(request.stock());
        if (request.category() != null) entity.setCategory(request.category());
    }
}