package product.management.Application;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

@Singleton
public class KafkaProducerService {

    private final String broker;
    private final String topic;
    private volatile KafkaProducer<String, String> producer;

    @Inject
    public KafkaProducerService(@Named("kafkaBroker") String broker,
                                @Named("kafkaTopic")  String topic) {
        this.broker = broker;
        this.topic  = topic;
        this.producer = createWithRetry();
    }

    private KafkaProducer<String, String> getProducer() {
        if (producer == null) {
            synchronized (this) {
                if (producer == null) {
                    producer = createWithRetry();
                }
            }
        }
        return producer;
    }

    private KafkaProducer<String, String> createWithRetry() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        int maxAttempts = 30;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                KafkaProducer<String, String> p = new KafkaProducer<>(props);
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

    public RecordMetadata send(String key, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        try {
            Future<RecordMetadata> future = getProducer().send(record);
            RecordMetadata metadata = future.get();
            System.out.printf("[KafkaProducer] Sent -> topic=%s partition=%d offset=%d%n",
                    metadata.topic(), metadata.partition(), metadata.offset());
            return metadata;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }

    public void close() {
        if (producer != null) producer.close();
    }
}
