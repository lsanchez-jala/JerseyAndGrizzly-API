package product.management.Domain.DTO.Product;

public record ProductRequest(
        String name,
        String sku,
        Float price,
        Integer stock,
        String category
) {
}
