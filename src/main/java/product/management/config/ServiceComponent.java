package product.management.config;

import dagger.Component;
import jakarta.inject.Singleton;
import org.glassfish.grizzly.http.server.HttpServer;
import product.management.API.*;
import product.management.Application.*;
import product.management.Application.impl.SchemaRegistryService;

@Singleton
@Component(modules = {
    AppModule.class,
    ApplicationModule.class,
    ApplicationAbstractModule.class,
    APIAbstractModule.class,
    APIModule.class
})
public interface ServiceComponent {

    HttpServer httpServer();
    SchemaRegistryService schemaRegistryService();

    @Component.Builder
    interface Builder {
        ServiceComponent build();
    }
}