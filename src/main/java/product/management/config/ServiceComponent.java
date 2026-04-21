package product.management.config;

import dagger.Component;
import jakarta.inject.Singleton;
import org.glassfish.grizzly.http.server.HttpServer;
import product.management.Application.impl.SchemaRegistryService;

@Singleton
@Component(modules = { AppModule.class, AppServicesModule.class })
public interface ServiceComponent {

    HttpServer httpServer();
    SchemaRegistryService schemaRegistryService();

    @Component.Builder
    interface Builder {
        ServiceComponent build();
    }
}