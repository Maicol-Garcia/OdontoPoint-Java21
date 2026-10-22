package pe.odontopoint.exception;

@org.springframework.web.bind.annotation.ResponseStatus(
    org.springframework.http.HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException() {
    super("El registro solicitado no existe.");
  }
}
