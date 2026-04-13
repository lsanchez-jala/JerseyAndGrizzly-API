package product.management.Domain.DTO.Customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerDTO(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String createdAt,
        String updatedAt
) {}