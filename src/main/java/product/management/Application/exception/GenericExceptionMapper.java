package product.management.Application.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        LOGGER.log(Level.SEVERE, "Unhandled exception", ex);
        ErrorResponse err = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Unexpected server error",
                uriInfo != null ? uriInfo.getPath() : "");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(err)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}