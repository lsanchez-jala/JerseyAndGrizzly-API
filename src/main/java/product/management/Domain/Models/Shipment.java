package product.management.Domain.Models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    private UUID id;
    @NotNull
    private UUID orderId;
    @NotNull
    private String trackingCode;
    @NotNull
    private String carrier;
    @NotNull
    private String status;
    @NotNull
    private Instant createdAt;
    @NotBlank
    private Instant updatedAt;
}
