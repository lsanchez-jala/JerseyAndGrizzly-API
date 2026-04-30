package product.management;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import product.management.API.*;
import product.management.Application.exception.GenericExceptionMapper;
import java.util.Set;

@Singleton
@ApplicationPath("/api/v1")
public class MyApplication extends ResourceConfig {
    @Inject
    public MyApplication(
            ProductResource productResource,
            OrderResource orderResource,
            CustomerResource customerResource,
            ShipmentResource shipmentResource,
            OrderItemResource orderItemResource,
            GreetingResource greetingResource
    ) {
//        packages("product.management.API");
        register(greetingResource);
        register(productResource);
        register(orderResource);
        register(customerResource);
        register(orderItemResource);
        register(shipmentResource);
        register(JacksonFeature.class);
        register(GenericExceptionMapper.class);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);

        // Register Swagger's built-in OpenAPI endpoint (/openapi.json + /openapi.yaml)
        register(OpenApiResource.class);
        register(SwaggerUiResource.class);
        // Configure the spec metadata
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Order Management API")
                        .version("1.0")
                        .description("REST API for the order management system"));

        SwaggerConfiguration config = new SwaggerConfiguration()
                .openAPI(openAPI)
                .prettyPrint(true)
                .resourcePackages(Set.of("product.management.API")); // your resources package

        try {
            new JaxrsOpenApiContextBuilder<>()
                    .application(this)
                    .openApiConfiguration(config)
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {

            throw new RuntimeException("Failed to init OpenAPI context", e);
        }
    }
}