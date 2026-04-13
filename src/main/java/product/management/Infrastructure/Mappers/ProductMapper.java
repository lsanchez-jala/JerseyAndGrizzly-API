package product.management.Infrastructure.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import product.management.Domain.DTO.Product.ProductDTO;
import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Domain.Models.Product;

@Mapper( nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    ProductDTO toDto(Product entity);
    void toEntity(ProductRequest request, @MappingTarget Product entity);
}