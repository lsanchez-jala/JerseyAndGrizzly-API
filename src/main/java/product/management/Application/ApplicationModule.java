package product.management.Application;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import product.management.Application.impl.KafkaProducerService;
import product.management.Infrastructure.Mappers.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Module
public class ApplicationModule {

    @Provides
    @Singleton
    public KafkaProducerService producerService(Properties props){
        return new KafkaProducerService(props);
    }

    @Provides
    @Singleton
    public CustomerMapper provideCustomerMapper() {
        return new CustomerMapper();
    }

    @Provides
    @Singleton
    public ProductMapper provideProductMapper() {
        return new ProductMapper();
    }

    @Provides
    @Singleton
    public OrderMapper provideOrderMapper() {
        return new OrderMapper();
    }

    @Provides
    @Singleton
    public OrderItemMapper provideOrderItemMapper() {
        return new OrderItemMapper();
    }

    @Provides
    @Singleton
    public ShipmentMapper provideShipmentMapper() {
        return new ShipmentMapper();
    }

}
