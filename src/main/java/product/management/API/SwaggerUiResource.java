package product.management.API;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;

@Path("/swagger-ui")
public class SwaggerUiResource {

    @GET
    @Path("/index.html")
    @Produces(MediaType.TEXT_HTML)
    public Response getIndex() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Order Management API</title>
                <meta charset="utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <link rel="stylesheet" type="text/css" href="swagger-ui.css" >
            </head>
            <body>
            <div id="swagger-ui"></div>
            <script src="swagger-ui-bundle.js"> </script>
            <script src="swagger-ui-standalone-preset.js"> </script>
            <script>
                window.onload = function() {
                    SwaggerUIBundle({
                        url: "/api/v1/openapi.json",
                        dom_id: '#swagger-ui',
                        presets: [
                            SwaggerUIBundle.presets.apis,
                            SwaggerUIStandalonePreset
                        ],
                        layout: "StandaloneLayout"
                    })
                }
            </script>
            </body>
            </html>
            """;
        return Response.ok(html).build();
    }

    @GET
    @Path("/{fileName: .*}")
    public Response getFile(@PathParam("fileName") String fileName) {
        String resourcePath = "/META-INF/resources/webjars/swagger-ui/5.17.14/" + fileName;
        InputStream stream = getClass().getResourceAsStream(resourcePath);
        if (stream == null) {
            return Response.status(404).build();
        }
        return Response.ok(stream).type(guessMediaType(fileName)).build();
    }

    private String guessMediaType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".js"))   return "application/javascript";
        if (fileName.endsWith(".css"))  return "text/css";
        if (fileName.endsWith(".png"))  return "image/png";
        return "application/octet-stream";
    }
}