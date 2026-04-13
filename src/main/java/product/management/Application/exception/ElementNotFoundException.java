package product.management.Application.exception;

import jakarta.ws.rs.core.Response;

public class ElementNotFoundException extends GenericException {
    public ElementNotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND); // 404
    }
}