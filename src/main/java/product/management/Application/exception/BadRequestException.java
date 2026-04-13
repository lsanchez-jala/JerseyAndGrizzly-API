package product.management.Application.exception;

import jakarta.ws.rs.core.Response;

public class BadRequestException extends GenericException {
    public BadRequestException(String message) {
        super(message, Response.Status.BAD_REQUEST);
    }
}
