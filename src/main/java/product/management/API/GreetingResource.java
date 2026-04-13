package product.management.API;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/hello")
public class GreetingResource {

    @Inject
    public GreetingResource() {

    }

    @GET
    public Response greet(@QueryParam("name") @DefaultValue("world") String name){
        String result = "hello "+ name;
        return Response.ok(result).build();
    }
}
