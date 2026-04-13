package product.management.Application.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class GenericException extends RuntimeException {
    private final Response.Status status;

    public GenericException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

}
