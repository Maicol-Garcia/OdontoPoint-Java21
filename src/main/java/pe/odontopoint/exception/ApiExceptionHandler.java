package pe.odontopoint.exception;

import java.util.Map;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice(
    assignableTypes = {
      pe.odontopoint.controller.PatientController.class,
      pe.odontopoint.controller.AgendaController.class,
      pe.odontopoint.controller.CatalogoController.class,
      pe.odontopoint.controller.DocumentController.class,
      pe.odontopoint.controller.ClinicalEvidenceController.class,
      pe.odontopoint.controller.PaymentReceiptController.class,
      pe.odontopoint.controller.ExportController.class,
      pe.odontopoint.controller.IngresosController.class
    })
public class ApiExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  ResponseEntity<?> business(BusinessException e) {
    return error(400, e.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<?> missing(NotFoundException e) {
    return error(404, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    return error(
        400,
        e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .findFirst()
            .orElse("Revise los datos."));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> integrity(DataIntegrityViolationException e) {
    String details =
        org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e).getMessage();
    String lower = details == null ? "" : details.toLowerCase(java.util.Locale.ROOT);
    if (lower.contains("unico_dni_historia")
        || (lower.contains("dni") && lower.contains("duplicate"))) {
      return error(409, "No se pudo guardar: el DNI ya está registrado en otra historia clínica.");
    }
    if (lower.contains("contenido")
        && (lower.contains("too long") || lower.contains("data too long"))) {
      return error(
          409,
          "La columna de imágenes de la base de datos es demasiado pequeña. Ejecute"
              + " sql/04-corregir-evidencias.sql y vuelva a intentarlo.");
    }
    return error(
        409,
        "La base de datos rechazó el registro por una restricción. Revise que la actualización SQL"
            + " del proyecto esté aplicada.");
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  ResponseEntity<?> conflict(Exception e) {
    return error(409, "El registro cambió en otra sesión. Recargue la página antes de editar.");
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<?> size(Exception e) {
    return error(413, "La imagen supera el tamaño máximo permitido.");
  }

  @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
  ResponseEntity<?> unreadable(Exception e) {
    return error(400, "El formato de los datos no es válido. Revise los importes y campos.");
  }

  private ResponseEntity<?> error(int status, String message) {
    return ResponseEntity.status(status).body(Map.of("message", message));
  }
}
