package product.management.Application;

import org.apache.avro.generic.GenericRecord;

public interface IKafkaProducerService {
    void send(String key, GenericRecord messageObject) ;
    void close();
}
