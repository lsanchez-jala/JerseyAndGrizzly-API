package product.management.config;

import dagger.Component;
import jakarta.inject.Singleton;
import org.glassfish.grizzly.http.server.HttpServer;

@Singleton
@Component(modules = { AppModule.class })
public interface ServiceComponent {

    HttpServer httpServer();

    @Component.Builder
    interface Builder {
        ServiceComponent build();
    }
}