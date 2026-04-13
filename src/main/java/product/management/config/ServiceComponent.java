package product.management.config;

import dagger.Component;
import jakarta.inject.Singleton;
import org.glassfish.grizzly.http.server.HttpServer;
import product.management.Infrastructure.Migrations.DatabaseMigrationManager;

@Singleton
@Component(modules = { AppModule.class })
public interface ServiceComponent {

    HttpServer httpServer();
    DatabaseMigrationManager migrationManager();

    @Component.Builder
    interface Builder {
        ServiceComponent build();
    }
}