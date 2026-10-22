package pe.odontopoint.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(
    name = "historias_clinicas",
    uniqueConstraints = @UniqueConstraint(name = "unico_dni_historia", columnNames = "dni"))
public class PatientHistory {
  @Id private Long id;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "fecha_actualizacion", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "creado_por", nullable = false, updatable = false, length = 255)
  private String createdBy;

  @Column(name = "actualizado_por", nullable = false, length = 255)
  private String updatedBy;

  @Column(name = "fecha_nacimiento")
  private LocalDate birthDate;

  @Column(name = "fecha_consentimiento")
  private LocalDate consentDate;

  @Column(name = "nombre_completo", length = 180)
  private String fullName = "";

  @Column(name = "dni", length = 8)
  private String dni = "";

  @Column(name = "sexo", length = 40)
  private String sex = "";

  @Column(name = "domicilio", length = 250)
  private String address = "";

  @Column(name = "lugar_procedencia", length = 150)
  private String origin = "";

  @Column(name = "ocupacion", length = 150)
  private String occupation = "";

  @Column(name = "estado_civil", length = 40)
  private String maritalStatus = "";

  @Column(name = "telefono", length = 40)
  private String phone = "";

  @Column(name = "nombre_apoderado", length = 180)
  private String guardianName = "";

  @Column(name = "dni_apoderado", length = 8)
  private String guardianDni = "";

  @Column(name = "telefono_apoderado", length = 40)
  private String guardianPhone = "";

  @Column(name = "motivo_consulta", columnDefinition = "TEXT")
  private String reason = "";

  @Column(name = "signos_sintomas", columnDefinition = "TEXT")
  private String symptoms = "";

  @Column(name = "tiempo_enfermedad", length = 180)
  private String illnessDuration = "";

  @Column(name = "detalle_alergias", columnDefinition = "TEXT")
  private String allergyDetails = "";

  @Column(name = "otros_antecedentes", columnDefinition = "TEXT")
  private String otherHistory = "";

  @Column(name = "examen_clinico", columnDefinition = "TEXT")
  private String clinicalExam = "";

  @Column(name = "diagnostico", columnDefinition = "TEXT")
  private String diagnosis = "";

  @Column(name = "observaciones_odontograma", columnDefinition = "TEXT")
  private String toothNotes = "";

  @Column(name = "leyenda_negro", length = 180)
  private String blackLegend = "";

  @Column(name = "leyenda_amarillo", length = 180)
  private String yellowLegend = "";

  @Column(name = "leyenda_verde", length = 180)
  private String greenLegend = "";

  @Column(name = "estado_consentimiento", length = 30)
  private String consentStatus = "";

  @Column(name = "detalle_consentimiento", columnDefinition = "TEXT")
  private String consentDetails = "";

  @Column(name = "detalle_revocacion", columnDefinition = "TEXT")
  private String revocation = "";

  @Column(name = "profesional_responsable", length = 180)
  private String professional = "";

  @ElementCollection
  @CollectionTable(name = "antecedentes_medicos", joinColumns = @JoinColumn(name = "id_historia"))
  @MapKeyColumn(name = "nombre_antecedente")
  @Column(name = "respuesta")
  private Map<String, String> conditions = new LinkedHashMap<>();

  @ElementCollection
  @CollectionTable(name = "marcas_odontograma", joinColumns = @JoinColumn(name = "id_historia"))
  @MapKeyColumn(name = "clave_superficie")
  @Column(name = "color_marca")
  private Map<String, String> teeth = new LinkedHashMap<>();

  @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  private List<Treatment> treatments = new ArrayList<>();

  @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  private List<Payment> payments = new ArrayList<>();

  public String number() {
    return String.format("HC-%06d", id);
  }

  public BigDecimal total() {
    return treatments.stream().map(Treatment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal paid() {
    return payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public Long getId() {
    return this.id;
  }

  public long getVersion() {
    return this.version;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return this.updatedAt;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public String getUpdatedBy() {
    return this.updatedBy;
  }

  public LocalDate getBirthDate() {
    return this.birthDate;
  }

  public LocalDate getConsentDate() {
    return this.consentDate;
  }

  public String getFullName() {
    return this.fullName;
  }

  public String getDni() {
    return this.dni;
  }

  public String getSex() {
    return this.sex;
  }

  public String getAddress() {
    return this.address;
  }

  public String getOrigin() {
    return this.origin;
  }

  public String getOccupation() {
    return this.occupation;
  }

  public String getMaritalStatus() {
    return this.maritalStatus;
  }

  public String getPhone() {
    return this.phone;
  }

  public String getGuardianName() {
    return this.guardianName;
  }

  public String getGuardianDni() {
    return this.guardianDni;
  }

  public String getGuardianPhone() {
    return this.guardianPhone;
  }

  public String getReason() {
    return this.reason;
  }

  public String getSymptoms() {
    return this.symptoms;
  }

  public String getIllnessDuration() {
    return this.illnessDuration;
  }

  public String getAllergyDetails() {
    return this.allergyDetails;
  }

  public String getOtherHistory() {
    return this.otherHistory;
  }

  public String getClinicalExam() {
    return this.clinicalExam;
  }

  public String getDiagnosis() {
    return this.diagnosis;
  }

  public String getToothNotes() {
    return this.toothNotes;
  }

  public String getBlackLegend() {
    return this.blackLegend;
  }

  public String getYellowLegend() {
    return this.yellowLegend;
  }

  public String getGreenLegend() {
    return this.greenLegend;
  }

  public String getConsentStatus() {
    return this.consentStatus;
  }

  public String getConsentDetails() {
    return this.consentDetails;
  }

  public String getRevocation() {
    return this.revocation;
  }

  public String getProfessional() {
    return this.professional;
  }

  public Map<String, String> getConditions() {
    return this.conditions;
  }

  public Map<String, String> getTeeth() {
    return this.teeth;
  }

  public List<Treatment> getTreatments() {
    return this.treatments;
  }

  public List<Payment> getPayments() {
    return this.payments;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public void setVersion(final long version) {
    this.version = version;
  }

  public void setCreatedAt(final LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(final LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }

  public void setUpdatedBy(final String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public void setBirthDate(final LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public void setConsentDate(final LocalDate consentDate) {
    this.consentDate = consentDate;
  }

  public void setFullName(final String fullName) {
    this.fullName = fullName;
  }

  public void setDni(final String dni) {
    this.dni = dni;
  }

  public void setSex(final String sex) {
    this.sex = sex;
  }

  public void setAddress(final String address) {
    this.address = address;
  }

  public void setOrigin(final String origin) {
    this.origin = origin;
  }

  public void setOccupation(final String occupation) {
    this.occupation = occupation;
  }

  public void setMaritalStatus(final String maritalStatus) {
    this.maritalStatus = maritalStatus;
  }

  public void setPhone(final String phone) {
    this.phone = phone;
  }

  public void setGuardianName(final String guardianName) {
    this.guardianName = guardianName;
  }

  public void setGuardianDni(final String guardianDni) {
    this.guardianDni = guardianDni;
  }

  public void setGuardianPhone(final String guardianPhone) {
    this.guardianPhone = guardianPhone;
  }

  public void setReason(final String reason) {
    this.reason = reason;
  }

  public void setSymptoms(final String symptoms) {
    this.symptoms = symptoms;
  }

  public void setIllnessDuration(final String illnessDuration) {
    this.illnessDuration = illnessDuration;
  }

  public void setAllergyDetails(final String allergyDetails) {
    this.allergyDetails = allergyDetails;
  }

  public void setOtherHistory(final String otherHistory) {
    this.otherHistory = otherHistory;
  }

  public void setClinicalExam(final String clinicalExam) {
    this.clinicalExam = clinicalExam;
  }

  public void setDiagnosis(final String diagnosis) {
    this.diagnosis = diagnosis;
  }

  public void setToothNotes(final String toothNotes) {
    this.toothNotes = toothNotes;
  }

  public void setBlackLegend(final String blackLegend) {
    this.blackLegend = blackLegend;
  }

  public void setYellowLegend(final String yellowLegend) {
    this.yellowLegend = yellowLegend;
  }

  public void setGreenLegend(final String greenLegend) {
    this.greenLegend = greenLegend;
  }

  public void setConsentStatus(final String consentStatus) {
    this.consentStatus = consentStatus;
  }

  public void setConsentDetails(final String consentDetails) {
    this.consentDetails = consentDetails;
  }

  public void setRevocation(final String revocation) {
    this.revocation = revocation;
  }

  public void setProfessional(final String professional) {
    this.professional = professional;
  }

  public void setConditions(final Map<String, String> conditions) {
    this.conditions = conditions;
  }

  public void setTeeth(final Map<String, String> teeth) {
    this.teeth = teeth;
  }

  public void setTreatments(final List<Treatment> treatments) {
    this.treatments = treatments;
  }

  public void setPayments(final List<Payment> payments) {
    this.payments = payments;
  }
}
