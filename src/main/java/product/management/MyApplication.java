package product.management;

import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import product.management.API.*;

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
        registerInstances(
                productResource,
                customerResource,
                orderResource,
                shipmentResource,
                orderItemResource,
                greetingResource
        );
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(greetingResource).to(GreetingResource.class);
                bind(productResource).to(ProductResource.class);
                bind(orderResource).to(OrderResource.class);
                bind(customerResource).to(CustomerResource.class);
                bind(orderItemResource).to(OrderItemResource.class);
                bind(shipmentResource).to(ShipmentResource.class);
            }
        });

        register(JacksonFeature.class);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
    }
}