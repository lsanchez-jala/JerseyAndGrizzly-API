package product.management.Domain.DTO.Order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreateRequest(
        @JsonProperty("customerId")
        String customerIdString,

        UUID customerId,
        UUID shipmentId
) {}
