package product.management.Infrastructure.Mappers;

import jakarta.inject.Singleton;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import product.management.Domain.DTO.Order.OrderDTO;
import product.management.Domain.DTO.Order.OrderCreateRequest;
import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Order;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Singleton
public class OrderMapper {

    public OrderDTO toDto(Order entity) {
        return new OrderDTO(
                entity.getId(),
                entity.getCustomerId(),
                entity.getShipmentId(),
                entity.getStatus().name(),
                entity.getTotalAmount(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt() != null
                        ? entity.getUpdatedAt().toString()
                        : null
        );
    }

    private static final Schema SCHEMA;

    static {
        try (InputStream is = OrderMapper.class
                .getResourceAsStream("/avro/order-dto.avsc")) {
            SCHEMA = new Schema.Parser().parse(is);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load OrderDTO Avro schema: " + e.getMessage());
        }
    }

    public GenericRecord toGenericRecord(OrderDTO dto) {
        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("id",          dto.id().toString());
        record.put("customerId",  dto.customerId() != null ?  dto.customerId().toString() : null);
        record.put("shipmentId",  dto.shipmentId() != null
                ? dto.shipmentId().toString()
                : null);
        record.put("status",      dto.status());
        record.put("totalAmount", dto.totalAmount());
        record.put("createdAt",   dto.createdAt());
        record.put("updatedAt",   dto.updatedAt());
        return record;
    }

    public void toEntity(OrderCreateRequest request, Order entity) {
        if (request.customerId() != null)   entity.setCustomerId(UUID.fromString(request.customerId()));
        if (request.shipmentId() != null)   entity.setShipmentId(UUID.fromString(request.shipmentId()));
    }
}