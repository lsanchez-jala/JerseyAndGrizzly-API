package product.management.config;

import dagger.Module;
import dagger.Provides;
import jakarta.annotation.Nonnull;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import product.management.Infrastructure.Mappers.*;
import product.management.Infrastructure.Repositories.*;
import product.management.MyApplication;

import javax.sql.DataSource;
import java.net.URI;

@Module
public class AppModule {
    @Provides
    @Singleton
    @Named("applicationPort")
    public Integer applicationPort() {
        return 8080;
    }

    @Provides
    @Singleton
    @Named("applicationBaseUri")
    public URI baseUri(
            @Named("applicationPort") @Nonnull final Integer applicationPort) {
        return UriBuilder.fromUri("http://0.0.0.0/").port(applicationPort).build();
    }

    @Provides
    @Singleton
    public HttpServer httpServer(
            @Named("applicationBaseUri") @Nonnull final URI applicationBaseUri,
            @Nonnull final MyApplication myResourceConfig) {
        return GrizzlyHttpServerFactory
                .createHttpServer(applicationBaseUri, myResourceConfig, false);
    }

    @Provides
    @Singleton
    public JacksonFeature jacksonFeature() {
        return new JacksonFeature();
    }

    @Provides
    @Singleton
    DataSource provideDataSource() {
        return DataSourceProvider.create();
    }


    // -------------------------------------------------------------------------
    // Repositories & Mappers
    // -------------------------------------------------------------------------

    // Customer
    @Provides
    @Singleton
    CustomerRepository provideCustomerRepository(DataSource ds) {
        return new CustomerRepository(ds);
    }

    @Provides
    @Singleton
    public CustomerMapper provideCustomerMapper() {
        return new CustomerMapper();
    }

    // Product
    @Provides
    @Singleton
    ProductRepository provideProductRepository(DataSource ds) {
        return new ProductRepository(ds);
    }

    @Provides
    @Singleton
    public ProductMapper provideProductMapper() {
        return new ProductMapper(); // MapStruct's registry
    }

    // Order
    @Provides
    @Singleton
    OrderRepository provideOrderRepository(DataSource ds) {
        return new OrderRepository(ds);
    }

    @Provides
    @Singleton
    public OrderMapper provideOrderMapper() {
        return new OrderMapper(); // MapStruct's registry
    }

    // OrderItem
    @Provides
    @Singleton
    OrderItemRepository provideOrderItemRepository(DataSource ds) {
        return new OrderItemRepository(ds);
    }

    @Provides
    @Singleton
    public OrderItemMapper provideOrderItemMapper() {
        return new OrderItemMapper();
    }

    // Shipment
    @Provides
    @Singleton
    ShipmentRepository provideShipmentRepository(DataSource ds) {
        return new ShipmentRepository(ds);
    }

    @Provides
    @Singleton
    public ShipmentMapper provideShipmentMapper() {
        return new ShipmentMapper();
    }

    // -------------------------------------------------------------------------
    // Kafka
    // -------------------------------------------------------------------------
    @Provides @Singleton @Named("kafkaBroker")
    public String kafkaBroker() {
        String env = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        return (env != null && !env.isBlank()) ? env : "localhost:9092";
    }

    @Provides @Singleton @Named("kafkaTopic")
    public String kafkaTopic() { return "app-events"; }

}
