package pe.odontopoint.mapper;

import org.springframework.stereotype.Component;
import pe.odontopoint.dto.HistoryView;
import pe.odontopoint.report.ReportData;

@Component
public class ReportMapper {
  public ReportData map(HistoryView view) {
    var r = new ReportData();
    var f = view.form();
    r.id = view.id();
    r.created = view.createdAt();
    r.updated = view.updatedAt();
    r.fields.put("Edad", String.valueOf(view.age()));
    r.fields.put("Nombre completo", f.getFullName());
    r.fields.put("Sexo", f.getSex());
    r.fields.put("DNI", f.getDni());
    r.fields.put("Fecha de nacimiento", f.getBirthDate());
    r.fields.put("Domicilio", f.getAddress());
    r.fields.put("Lugar de procedencia", f.getOrigin());
    r.fields.put("Ocupación", f.getOccupation());
    r.fields.put("Estado civil", f.getMaritalStatus());
    r.fields.put("Teléfono", f.getPhone());
    r.fields.put("Apoderado: nombre", f.getGuardianName());
    r.fields.put("Apoderado: DNI", f.getGuardianDni());
    r.fields.put("Apoderado: teléfono", f.getGuardianPhone());
    r.fields.put("Motivo de consulta", f.getReason());
    r.fields.put("Signos y síntomas principales", f.getSymptoms());
    r.fields.put("Tiempo de enfermedad", f.getIllnessDuration());
    r.fields.put("Alergia: ¿a qué?", f.getAllergyDetails());
    r.fields.put("Otros antecedentes", f.getOtherHistory());
    r.fields.put("Examen clínico", f.getClinicalExam());
    r.fields.put("Diagnóstico", f.getDiagnosis());
    r.fields.put("Observaciones del odontograma", f.getToothNotes());
    r.fields.put("Leyenda negro", f.getBlackLegend());
    r.fields.put("Leyenda amarillo", f.getYellowLegend());
    r.fields.put("Leyenda verde", f.getGreenLegend());
    r.fields.put("Estado del consentimiento", f.getConsentStatus());
    r.fields.put("Detalle del consentimiento", f.getConsentDetails());
    r.fields.put("Fecha del consentimiento", f.getConsentDate());
    r.fields.put("Revocatoria", f.getRevocation());
    r.fields.put("Profesional tratante", f.getProfessional());

    r.conditions.putAll(f.getConditions());
    r.teeth.putAll(f.getTeeth());
    for (var t : f.getTreatments())
      r.treatments.add(new ReportData.Treatment(t.description(), t.amount()));
    for (var a : f.getPayments())
      r.payments.add(new ReportData.Payment(a.date(), a.amount(), a.receiptName(), a.signer()));
    return r;
  }
}
