package product.management;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import product.management.API.*;
import product.management.Application.exception.AppExceptionMapper;

import java.util.Properties;

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
        packages("product.management.API");
        register(greetingResource);
        register(productResource);
        register(orderResource);
        register(customerResource);
        register(orderItemResource);
        register(shipmentResource);
        register(JacksonFeature.class);
        register(AppExceptionMapper.class);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
    }
}