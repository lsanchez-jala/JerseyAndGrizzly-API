package product.management.Application.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private Response.Status status;
    private String message;
    private String path;

    public ErrorResponse(Response.Status status, String message, String path) {
        this.status = status; this.message = message; this.path = path;
    }

}