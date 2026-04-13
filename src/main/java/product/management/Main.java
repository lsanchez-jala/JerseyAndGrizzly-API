package product.management;

import org.glassfish.grizzly.http.server.HttpServer;
import product.management.Infrastructure.Migrations.DatabaseMigrationManager;
import product.management.config.DaggerServiceComponent;
import product.management.config.ServiceComponent;

import java.io.IOException;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws IOException, InterruptedException {
        final ServiceComponent component = DaggerServiceComponent.builder().build();

        DatabaseMigrationManager dbManager = component.migrationManager();
        dbManager.run();

        HttpServer server = component.httpServer();
        server.start();

        System.out.println("Server started at: " + BASE_URI);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
            System.out.println("Server stopped.");
        }));

        Thread.currentThread().join();
    }
}