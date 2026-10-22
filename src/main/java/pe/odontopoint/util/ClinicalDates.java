package pe.odontopoint.util;

import java.time.*;
import java.time.format.*;
import pe.odontopoint.exception.BusinessException;

public final class ClinicalDates {
  private ClinicalDates() {}

  public static final ZoneId ZONE = ZoneId.of("America/Lima");
  public static final DateTimeFormatter DATE =
      DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

  public static String format(LocalDate d) {
    return d == null ? "" : DATE.format(d);
  }

  public static String format(LocalDateTime d) {
    return d == null ? "" : d.format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm"));
  }

  public static LocalDate parse(String value, String label) {
    try {
      return LocalDate.parse(value, DATE);
    } catch (Exception e) {
      throw new BusinessException(label + ": use una fecha válida en formato dd/mm/aaaa.");
    }
  }

  public static LocalDate today() {
    return LocalDate.now(ZONE);
  }

  public static LocalDateTime now() {
    return LocalDateTime.now(ZONE);
  }
}
