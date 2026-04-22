package API;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import product.management.API.GreetingResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GreetingsResourceIntegrationTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(GreetingResource.class);
    }


    @Test
    public void givenGetHiGreeting_whenCorrectRequest_thenResponseIsOkAndContainsHello() {
        Response response = target("/hello").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

        String content = response.readEntity(String.class);
        assertEquals("hello world", content);
    }

}
