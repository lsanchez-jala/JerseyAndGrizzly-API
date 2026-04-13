package product.management.Domain.DTO.Product;

public record ProductRequest(
        String name,
        String sku,
        Float price,
        int stock,
        String category
) {
}
