package product.management.Domain.DTO.Product;

import java.util.UUID;

public record ProductDTO(
        UUID id,
        String name,
        String sku,
        Float price,
        int stock,
        String category
) {
}
