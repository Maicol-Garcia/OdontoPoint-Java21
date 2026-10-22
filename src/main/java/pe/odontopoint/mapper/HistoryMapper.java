package pe.odontopoint.mapper;

import java.util.*;
import org.springframework.stereotype.Component;
import pe.odontopoint.dto.*;
import pe.odontopoint.entity.*;
import pe.odontopoint.exception.BusinessException;
import pe.odontopoint.util.ClinicalDates;

@Component
public class HistoryMapper {
  public void apply(HistoryRequest f, PatientHistory h) {
    h.setFullName(f.getFullName() == null ? "" : f.getFullName().trim());
    h.setDni(f.getDni() == null ? "" : f.getDni().trim());
    h.setSex(f.getSex() == null ? "" : f.getSex().trim());
    h.setAddress(f.getAddress() == null ? "" : f.getAddress().trim());
    h.setOrigin(f.getOrigin() == null ? "" : f.getOrigin().trim());
    h.setOccupation(f.getOccupation() == null ? "" : f.getOccupation().trim());
    h.setMaritalStatus(f.getMaritalStatus() == null ? "" : f.getMaritalStatus().trim());
    h.setPhone(f.getPhone() == null ? "" : f.getPhone().trim());
    h.setGuardianName(f.getGuardianName() == null ? "" : f.getGuardianName().trim());
    h.setGuardianDni(f.getGuardianDni() == null ? "" : f.getGuardianDni().trim());
    h.setGuardianPhone(f.getGuardianPhone() == null ? "" : f.getGuardianPhone().trim());
    h.setReason(f.getReason() == null ? "" : f.getReason().trim());
    h.setSymptoms(f.getSymptoms() == null ? "" : f.getSymptoms().trim());
    h.setIllnessDuration(f.getIllnessDuration() == null ? "" : f.getIllnessDuration().trim());
    h.setAllergyDetails(f.getAllergyDetails() == null ? "" : f.getAllergyDetails().trim());
    h.setOtherHistory(f.getOtherHistory() == null ? "" : f.getOtherHistory().trim());
    h.setClinicalExam(f.getClinicalExam() == null ? "" : f.getClinicalExam().trim());
    h.setDiagnosis(f.getDiagnosis() == null ? "" : f.getDiagnosis().trim());
    h.setToothNotes(f.getToothNotes() == null ? "" : f.getToothNotes().trim());
    h.setBlackLegend(f.getBlackLegend() == null ? "" : f.getBlackLegend().trim());
    h.setYellowLegend(f.getYellowLegend() == null ? "" : f.getYellowLegend().trim());
    h.setGreenLegend(f.getGreenLegend() == null ? "" : f.getGreenLegend().trim());
    h.setConsentStatus(f.getConsentStatus() == null ? "" : f.getConsentStatus().trim());
    h.setConsentDetails(f.getConsentDetails() == null ? "" : f.getConsentDetails().trim());
    h.setRevocation(f.getRevocation() == null ? "" : f.getRevocation().trim());
    h.setProfessional(f.getProfessional() == null ? "" : f.getProfessional().trim());

    h.setBirthDate(ClinicalDates.parse(f.getBirthDate(), "Nacimiento"));
    h.setConsentDate(
        f.getConsentDate() == null || f.getConsentDate().isBlank()
            ? null
            : ClinicalDates.parse(f.getConsentDate(), "Consentimiento"));
    h.getConditions().clear();
    h.getConditions().putAll(f.getConditions());
    h.getTeeth().clear();
    h.getTeeth().putAll(f.getTeeth());

    h.getTreatments().clear();
    for (var input : f.getTreatments()) {
      var t = new Treatment();
      t.setHistory(h);
      t.setDescription(input.description().trim());
      t.setAmount(input.amount());
      h.getTreatments().add(t);
    }

    // Los pagos conservan su ID para que un comprobante adjunto nunca se pierda
    // al volver a guardar otros datos de la historia clínica.
    Map<Long, Payment> existing = new LinkedHashMap<>();
    for (var payment : h.getPayments()) {
      if (payment.getId() != null) existing.put(payment.getId(), payment);
    }
    Set<Long> requested = new HashSet<>();
    List<Payment> newPayments = new ArrayList<>();
    for (var input : f.getPayments()) {
      Payment payment;
      if (input.id() != null) {
        payment = existing.get(input.id());
        if (payment == null || !requested.add(input.id()))
          throw new BusinessException("Se recibió un pago inválido o duplicado.");
      } else {
        payment = new Payment();
        payment.setHistory(h);
        newPayments.add(payment);
      }
      payment.setPaymentDate(ClinicalDates.parse(input.date(), "Pago"));
      payment.setAmount(input.amount());
      payment.setSigner(input.signer() == null ? "" : input.signer().trim());
    }
    h.getPayments().removeIf(p -> p.getId() != null && !requested.contains(p.getId()));
    h.getPayments().addAll(newPayments);
  }

  public HistoryView view(PatientHistory h) {
    var f = new HistoryRequest();
    f.setVersion(h.getVersion());
    f.setFullName(h.getFullName());
    f.setDni(h.getDni());
    f.setSex(h.getSex());
    f.setAddress(h.getAddress());
    f.setOrigin(h.getOrigin());
    f.setOccupation(h.getOccupation());
    f.setMaritalStatus(h.getMaritalStatus());
    f.setPhone(h.getPhone());
    f.setGuardianName(h.getGuardianName());
    f.setGuardianDni(h.getGuardianDni());
    f.setGuardianPhone(h.getGuardianPhone());
    f.setReason(h.getReason());
    f.setSymptoms(h.getSymptoms());
    f.setIllnessDuration(h.getIllnessDuration());
    f.setAllergyDetails(h.getAllergyDetails());
    f.setOtherHistory(h.getOtherHistory());
    f.setClinicalExam(h.getClinicalExam());
    f.setDiagnosis(h.getDiagnosis());
    f.setToothNotes(h.getToothNotes());
    f.setBlackLegend(h.getBlackLegend());
    f.setYellowLegend(h.getYellowLegend());
    f.setGreenLegend(h.getGreenLegend());
    f.setConsentStatus(h.getConsentStatus());
    f.setConsentDetails(h.getConsentDetails());
    f.setRevocation(h.getRevocation());
    f.setProfessional(h.getProfessional());

    f.setBirthDate(ClinicalDates.format(h.getBirthDate()));
    f.setConsentDate(ClinicalDates.format(h.getConsentDate()));
    f.setConditions(new LinkedHashMap<>(h.getConditions()));
    f.setTeeth(new LinkedHashMap<>(h.getTeeth()));
    f.setTreatments(
        h.getTreatments().stream()
            .map(t -> new HistoryRequest.TreatmentInput(t.getDescription(), t.getAmount()))
            .toList());
    f.setPayments(
        h.getPayments().stream()
            .map(
                p ->
                    new HistoryRequest.PaymentInput(
                        p.getId(),
                        ClinicalDates.format(p.getPaymentDate()),
                        p.getAmount(),
                        p.getSigner(),
                        p.getReceipt(),
                        p.getReceiptFile() != null))
            .toList());
    return new HistoryView(
        h.getId(),
        h.number(),
        h.getVersion(),
        ClinicalDates.format(h.getCreatedAt()),
        ClinicalDates.format(h.getUpdatedAt()),
        java.time.Period.between(h.getBirthDate(), ClinicalDates.today()).getYears(),
        h.total(),
        h.paid(),
        h.total().subtract(h.paid()),
        f);
  }
}
