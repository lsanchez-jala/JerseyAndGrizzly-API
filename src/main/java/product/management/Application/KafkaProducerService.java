package product.management.Application;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.concurrent.Future;

@Singleton
public class KafkaProducerService {

    private final String broker;
    private final String topic;
    private final String registry;
    private volatile KafkaProducer<String, GenericRecord> producer;

    @Inject
    public KafkaProducerService(Properties properties) {
        this.broker = properties.getProperty("jersey.kafka.broker");
        this.topic  = properties.getProperty("jersey.kafka.topic");
        this.registry = properties.getProperty("kafka.schema.registry.url");
        this.producer = createWithRetry();
    }

    private KafkaProducer<String, GenericRecord> getProducer() {
        if (producer == null) {
            synchronized (this) {
                if (producer == null) {
                    producer = createWithRetry();
                }
            }
        }
        return producer;
    }

    private KafkaProducer<String, GenericRecord> createWithRetry() {
        Properties props = getProperties();
        int maxAttempts = 30;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                KafkaProducer<String, GenericRecord> p = new KafkaProducer<>(props);
                // Force a metadata fetch to confirm connectivity
                p.partitionsFor(topic);
                System.out.println("[KafkaProducer] Connected to broker: " + broker);
                return p;
            } catch (Exception e) {
                System.out.printf("[KafkaProducer] Attempt %d/%d — broker not ready (%s). Retrying in 1 s...%n",
                        i, maxAttempts, e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for Kafka", ie);
                }
            }
        }
        throw new RuntimeException("Kafka broker unreachable after " + maxAttempts + " attempts: " + broker);
    }

    @NotNull
    private Properties getProperties() {
        Properties props = new Properties();
        // Kafka properties
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, "10");
        // Avro properties
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.setProperty("schema.registry.url", registry);
        props.put("auto.register.schemas", true);
        return props;
    }

    public void send(String key, GenericRecord messageObject) {
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topic, key, messageObject);
        try {
            Future<RecordMetadata> future = getProducer().send(record);
            RecordMetadata metadata = future.get();
            System.out.printf("[KafkaProducer] Sent -> topic=%s partition=%d offset=%d%n",
                    metadata.topic(), metadata.partition(), metadata.offset());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }

    public void close() {
        if (producer != null) producer.close();
    }
}
