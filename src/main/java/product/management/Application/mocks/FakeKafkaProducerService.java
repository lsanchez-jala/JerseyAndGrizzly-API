package product.management.Application.mocks;

import org.apache.avro.generic.GenericRecord;
import product.management.Application.IKafkaProducerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FakeKafkaProducerService implements IKafkaProducerService {

    public record SentMessage(String key, GenericRecord record) {}

    private final List<SentMessage> sentMessages = new ArrayList<>();

    @Override
    public void send(String key, GenericRecord record) {
        sentMessages.add(new SentMessage(key, record));
    }

    @Override
    public void close() {

    }

    public List<SentMessage> getSentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }

    public List<String> getSentKeys() {
        return sentMessages.stream()
                .map(SentMessage::key)
                .toList();
    }

    public boolean wasCalled() {
        return !sentMessages.isEmpty();
    }

    public boolean wasCalledWith(String key) {
        return sentMessages.stream()
                .anyMatch(m -> key.equals(m.key()));
    }

    public int callCount() {
        return sentMessages.size();
    }

    public void reset() {
        sentMessages.clear();
    }
}
