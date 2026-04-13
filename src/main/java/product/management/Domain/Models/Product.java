package product.management.Domain.Models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private UUID id;
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    @NotBlank
    private String sku;
    @NotNull
    private Float price;
    @NotNull
    private int stock;
    @NotNull
    @NotBlank
    private String category;
}
