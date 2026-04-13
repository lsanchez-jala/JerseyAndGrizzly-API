package product.management.Application.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception ex) {
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                .entity("An unexpected error occurred.")
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}