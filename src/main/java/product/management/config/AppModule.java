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
import product.management.Infrastructure.Repositories.impl.*;
import product.management.MyApplication;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

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
    public Properties provideProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is == null) throw new RuntimeException("application.properties not found");
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
        return props;
    }

    @Provides
    @Singleton
    DataSource provideDataSource(Properties props) {
        return DataSourceProvider.create(props);
    }

}
