package pe.odontopoint.report;

import java.util.*;

public class ReportData {
  public long id, revision;
  public String created = "", updated = "";
  public Map<String, String> fields = new LinkedHashMap<>();
  public Map<String, String> conditions = new LinkedHashMap<>();
  public Map<String, String> teeth = new TreeMap<>();
  public List<Treatment> treatments = new ArrayList<>();
  public List<Payment> payments = new ArrayList<>();

  public String value(String key) {
    return fields.getOrDefault(key, "");
  }

  public String number() {
    return id == 0 ? "Pendiente de guardar" : String.format("HC-%06d", id);
  }

  public record Treatment(String description, java.math.BigDecimal amount) {}

  public record Payment(String date, java.math.BigDecimal amount, String receipt, String signer) {}

  public java.math.BigDecimal total() {
    return treatments.stream()
        .map(Treatment::amount)
        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
  }

  public java.math.BigDecimal paid() {
    return payments.stream()
        .map(Payment::amount)
        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
  }
}
