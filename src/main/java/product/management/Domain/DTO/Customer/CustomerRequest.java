package product.management.Domain.DTO.Customer;

public record CustomerRequest(
        String firstName,
        String lastName,
        String email
) {}