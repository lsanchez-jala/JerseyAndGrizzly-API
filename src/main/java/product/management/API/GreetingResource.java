package product.management.API;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Path("/hello")
public class GreetingResource {

    private final Logger logger = LoggerFactory.getLogger(GreetingResource.class);

    @Inject
    public GreetingResource() {

    }

    @GET
    public Response greet(@QueryParam("name") @DefaultValue("world") String name, @Context UriInfo uriInfo){
        String result = "hello "+ name;
        URI location = uriInfo.getRequestUri();
        logger.info("GET {}", location.toString());
        return Response.ok(result).build();
    }
}
