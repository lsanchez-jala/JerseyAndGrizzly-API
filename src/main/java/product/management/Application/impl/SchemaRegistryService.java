package product.management.Application.impl;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import product.management.Application.ISchemaRegistryService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Singleton
public class SchemaRegistryService implements ISchemaRegistryService {

    private final SchemaRegistryClient client;
    private static final Logger logger = LoggerFactory.getLogger(SchemaRegistryService.class);
    @Inject
    public SchemaRegistryService(Properties props) {
        this.client = new CachedSchemaRegistryClient(
                props.getProperty("kafka.schema.registry.url"), 100
        );
    }

    public void registerAll() {
        List.of(
                "/avro/order-dto.avsc",
                "/avro/shipment-dto.avsc"
        ).forEach(this::register);
    }

    private void register(String resourcePath) {
        try {
            String schemaJson = new String(
                    Objects.requireNonNull(SchemaRegistryService.class.getResourceAsStream(resourcePath)).readAllBytes()
            );
            String subject = resolveSubject(resourcePath); // e.g. "order-dto-value"
            Schema schema = new Schema.Parser().parse(schemaJson);
            client.register(subject, new AvroSchema(schema));
            logger.info("Schema registered for subject {}", subject);
        } catch (IOException | RestClientException e) {
            throw new RuntimeException("Failed to register schema: " + resourcePath, e);
        }
    }

    private String resolveSubject(String resourcePath) {
        String filename = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        return filename.replace(".avsc", "-value");
    }
}
