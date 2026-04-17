package product.management.config;

import dagger.Component;
import jakarta.inject.Singleton;
import org.glassfish.grizzly.http.server.HttpServer;
import product.management.Application.SchemaRegistryService;

@Singleton
@Component(modules = { AppModule.class })
public interface ServiceComponent {

    HttpServer httpServer();
    SchemaRegistryService schemaRegistryService();

    @Component.Builder
    interface Builder {
        ServiceComponent build();
    }
}