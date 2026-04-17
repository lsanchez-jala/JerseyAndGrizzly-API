package product.management.Application.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.logging.Level;


@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    public static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        ErrorResponse err = new ErrorResponse(null, null, null);
        if (ex instanceof BadRequestException)
        {
            return getResponse(ex, err, Response.Status.BAD_REQUEST);
        }
        if (ex instanceof ElementNotFoundException){
            return getResponse(ex, err, Response.Status.NOT_FOUND);
        }
        if (ex instanceof IllegalArgumentException){
            return getResponse(ex, err, Response.Status.BAD_REQUEST);
        }
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                .entity("An unexpected error occurred.")
                .type(MediaType.TEXT_PLAIN)
                .build();
    }

    private Response getResponse(Throwable ex, ErrorResponse err, Response.Status status) {
        LOGGER.log(Level.SEVERE, "Exception threw", ex);
        err.setStatus(status);
        err.setMessage(ex.getMessage());
        err.setPath(uriInfo != null ? uriInfo.getPath() : "");
        return Response.status(status)
                .entity(err)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}