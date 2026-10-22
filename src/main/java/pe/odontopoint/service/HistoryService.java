package pe.odontopoint.service;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.odontopoint.dto.*;
import pe.odontopoint.entity.*;
import pe.odontopoint.exception.*;
import pe.odontopoint.mapper.HistoryMapper;
import pe.odontopoint.repository.*;
import pe.odontopoint.util.ClinicalDates;

@Service
@Transactional(readOnly = true)
public class HistoryService {
  private final PatientHistoryRepository histories;
  private final SequenceCounterRepository counters;
  private final HistoryMapper mapper;
  private final AuditService audit;
  public static final List<String> CONDITIONS =
      List.of(
          "Alergia",
          "Fiebre reumática",
          "Anemia",
          "Diabetes",
          "Tuberculosis",
          "Hepatitis infecciosa",
          "Infección venérea",
          "Enfermedad cardíaca",
          "Gastritis");

  public HistoryView find(Long id) {
    return mapper.view(entity(id));
  }

  public PatientHistory entity(Long id) {
    return histories.findById(id).orElseThrow(NotFoundException::new);
  }

  public Page<HistoryView> search(String q, int page) {
    return histories
        .search(q, PageRequest.of(Math.max(0, page), 12, Sort.by("id").descending()))
        .map(mapper::view);
  }

  public long count() {
    return histories.count();
  }

  public long thisMonth() {
    return histories.countByCreatedAtGreaterThanEqual(
        ClinicalDates.today().withDayOfMonth(1).atStartOfDay());
  }

  public List<HistoryView> all() {
    return histories.findAll(Sort.by("id")).stream().map(mapper::view).toList();
  }

  @Transactional
  public HistoryView save(Long id, HistoryRequest f, String actor) {
    validate(f);
    if (histories.existsByDniAndIdNot(f.getDni().trim(), id == null ? -1L : id))
      throw new BusinessException("Ya existe una historia con este DNI.");
    PatientHistory h;
    if (id == null) {
      var counter = counters.locked("historia");
      counter.setNextValue(counter.getNextValue() + 1);
      h = new PatientHistory();
      h.setId(counter.getNextValue());
      h.setCreatedAt(ClinicalDates.now());
      h.setCreatedBy(actor);
    } else {
      h = entity(id);
      if (h.getVersion() != f.getVersion())
        throw new BusinessException(
            "Otra sesión modificó esta historia. Recargue antes de guardar.");
    }
    mapper.apply(f, h);
    h.setUpdatedAt(ClinicalDates.now());
    h.setUpdatedBy(actor);
    h = histories.saveAndFlush(h);
    audit.record(h.getId(), id == null ? "HISTORIA CREADA" : "HISTORIA ACTUALIZADA", actor);
    return mapper.view(h);
  }

  private void validate(HistoryRequest f) {
    var birth = ClinicalDates.parse(f.getBirthDate(), "Nacimiento");
    var today = ClinicalDates.today();
    if (birth.isAfter(today) || birth.isBefore(today.minusYears(125)))
      throw new BusinessException("Revise la fecha de nacimiento.");
    if (!Set.of("Femenino", "Masculino", "Otro / no especificado").contains(f.getSex()))
      throw new BusinessException("Seleccione el sexo.");
    if (java.time.Period.between(birth, today).getYears() < 18
        && (blank(f.getGuardianName())
            || f.getGuardianDni() == null
            || !f.getGuardianDni().matches("[0-9]{8}")
            || blank(f.getGuardianPhone())))
      throw new BusinessException("Para menores complete nombre, DNI y teléfono del apoderado.");
    if (blank(f.getReason())) throw new BusinessException("Ingrese el motivo de consulta.");
    if (!Set.of("Pendiente", "Otorgado", "Revocado").contains(f.getConsentStatus()))
      throw new BusinessException("Seleccione el estado del consentimiento.");
    if (!f.getConsentStatus().equals("Pendiente")) {
      if (blank(f.getConsentDetails()) || blank(f.getProfessional()))
        throw new BusinessException("Complete el detalle del consentimiento y el profesional.");
      if (ClinicalDates.parse(f.getConsentDate(), "Consentimiento").isAfter(today))
        throw new BusinessException("La fecha de consentimiento no puede ser futura.");
      if (f.getConsentStatus().equals("Revocado") && blank(f.getRevocation()))
        throw new BusinessException("Registre el detalle y fecha de la revocatoria.");
    }
    f.getConditions()
        .forEach(
            (k, v) -> {
              if (!CONDITIONS.contains(k) || !Set.of("Sí", "No", "No informado").contains(v))
                throw new BusinessException("Antecedente inválido.");
            });
    f.getTeeth()
        .forEach(
            (key, color) -> {
              if (!validTooth(key) || !Set.of("NEGRO", "AMARILLO", "VERDE").contains(color))
                throw new BusinessException("Marca dental inválida.");
            });
    BigDecimal total = BigDecimal.ZERO;
    BigDecimal paid = BigDecimal.ZERO;
    for (var t : f.getTreatments()) {
      if (t.amount() == null
          || t.amount().signum() < 0
          || t.amount().scale() > 2
          || blank(t.description())) throw new BusinessException("Tratamiento inválido.");
      total = total.add(t.amount());
    }
    for (var a : f.getPayments()) {
      if (a.amount() == null || a.amount().signum() <= 0 || a.amount().scale() > 2)
        throw new BusinessException("Pago inválido.");
      if (ClinicalDates.parse(a.date(), "Pago").isAfter(today))
        throw new BusinessException("La fecha de pago no puede ser futura.");
      paid = paid.add(a.amount());
    }
    if (paid.compareTo(total) > 0) throw new BusinessException("Los pagos superan el presupuesto.");
  }

  private boolean blank(String s) {
    return s == null || s.isBlank();
  }

  private boolean validTooth(String key) {
    try {
      var a = key.split(":");
      int n = Integer.parseInt(a[0]);
      int q = n / 10;
      int t = n % 10;
      return a.length == 2
          && q >= 1
          && q <= 8
          && t >= 1
          && t <= (q <= 4 ? 8 : 5)
          && Set.of("V", "L", "M", "D", "O").contains(a[1]);
    } catch (Exception e) {
      return false;
    }
  }

  public HistoryService(
      final PatientHistoryRepository histories,
      final SequenceCounterRepository counters,
      final HistoryMapper mapper,
      final AuditService audit) {
    this.histories = histories;
    this.counters = counters;
    this.mapper = mapper;
    this.audit = audit;
  }
}
