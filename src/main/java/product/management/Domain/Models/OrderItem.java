package product.management.Domain.Models;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private UUID id;
    @NotNull
    private UUID orderId;
    @NotNull
    private UUID productId;
    @NotNull
    private int quantity;
    @NotNull
    private Float unitPrice;
}
