package product.management.Application.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private int status;
    private String message;
    private String path;

    public ErrorResponse() {}

    public ErrorResponse(int status, String message, String path) {
        this.status = status; this.message = message; this.path = path;
    }

}