package product.management.Domain.Models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import product.management.Domain.Enums.OrderStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order{

    private UUID id;
    @NotNull
    @NotBlank
    private UUID customerId;
    @NotNull
    @NotBlank
    private UUID shipmentId;
    @NotNull
    private OrderStatus status;
    @NotNull
    private Float totalAmount;
    @NotNull
    private Instant createdAt;
    @NotBlank
    private Instant updatedAt;

}
