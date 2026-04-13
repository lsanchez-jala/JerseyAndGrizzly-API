package product.management.Application.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundMapper implements ExceptionMapper<ElementNotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ElementNotFoundException ex) {
        ErrorResponse err = new ErrorResponse(
                Response.Status.NOT_FOUND.getStatusCode(),
                ex.getMessage(),
                uriInfo.getPath());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(err)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}