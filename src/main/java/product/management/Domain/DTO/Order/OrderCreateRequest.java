package product.management.Domain.DTO.Order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreateRequest(
        @JsonProperty("customerId")
        @Nullable
        String customerId,
        @Nullable
        @JsonProperty("shipmentId")
        String shipmentId
) {}
