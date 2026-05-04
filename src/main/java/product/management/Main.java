package product.management;

import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import product.management.API.APIComponent;
import product.management.API.DaggerAPIComponent;
import product.management.Application.ApplicationComponent;
import product.management.Application.DaggerApplicationComponent;
import product.management.config.DaggerServiceComponent;
import product.management.config.ServiceComponent;

import java.io.IOException;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        final ServiceComponent serviceComponent = DaggerServiceComponent.builder().build();
        final ApplicationComponent appComponent = DaggerApplicationComponent.builder().build();
        final APIComponent apiComponent = DaggerAPIComponent.builder().build();

        serviceComponent.schemaRegistryService().registerAll();

        HttpServer server = serviceComponent.httpServer();
        server.start();

        logger.info("Server started at: {}", BASE_URI);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
            logger.info("Server stopped.");
        }));

        Thread.currentThread().join();
    }
}