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
public class AppExceptionMapper implements ExceptionMapper<GenericException> {

    private static final Logger LOGGER = Logger.getLogger(AppExceptionMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(GenericException ex) {
        LOGGER.log(Level.SEVERE, "Exception threw", ex);
        ErrorResponse err = new ErrorResponse(
                ex.getStatus().getStatusCode(),
                ex.getMessage(),
                uriInfo != null ? uriInfo.getPath() : "");
        return Response.status(ex.getStatus().getStatusCode())
                .entity(err)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}