package product.management.Infrastructure.Mappers;

import jakarta.inject.Singleton;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import product.management.Domain.DTO.Shipment.ShipmentDTO;
import product.management.Domain.DTO.Shipment.ShipmentRequest;
import product.management.Domain.Enums.ShipmentStatus;
import product.management.Domain.Models.Shipment;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class ShipmentMapper {

    public ShipmentDTO toDto(Shipment entity) {
        return new ShipmentDTO(
                entity.getId(),
                entity.getTrackingCode(),
                entity.getCarrier(),
                entity.getStatus().name(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt() != null
                        ? entity.getUpdatedAt().toString()
                        : null
        );
    }

    private static final Schema SCHEMA;

    static {
        try (InputStream is = OrderMapper.class
                .getResourceAsStream("/avro/shipment-dto.avsc")) {
            SCHEMA = new Schema.Parser().parse(is);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load OrderDTO Avro schema: " + e.getMessage());
        }
    }

    public GenericRecord toGenericRecord(ShipmentDTO dto) {
        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("id",           dto.id().toString());
        record.put("trackingCode", dto.trackingCode());
        record.put("carrier",      dto.carrier());
        record.put("status",       dto.status());
        record.put("createdAt",    dto.createdAt());
        record.put("updatedAt",    dto.updatedAt());
        return record;
    }

    public void toEntity(ShipmentRequest request, Shipment entity) {
        if (request.carrier() != null)       entity.setCarrier(request.carrier());
        if (request.status() != null)        entity.setStatus(ShipmentStatus.valueOf(request.status()));
    }
}
