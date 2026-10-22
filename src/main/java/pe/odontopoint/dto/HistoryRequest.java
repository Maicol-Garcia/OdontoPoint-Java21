package pe.odontopoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

public class HistoryRequest {
  private long version;

  @NotBlank(message = "Ingrese el nombre completo.")
  @Size(max = 180)
  private String fullName = "";

  @Pattern(regexp = "[0-9]{8}", message = "El DNI debe tener 8 dígitos.")
  @NotBlank
  private String dni = "";

  @NotBlank private String sex = "";

  @NotBlank(message = "Ingrese la fecha de nacimiento (dd/mm/aaaa).")
  private String birthDate = "";

  private String consentDate = "";

  @Size(max = 250)
  private String address = "";

  @Size(max = 150)
  private String origin = "";

  @Size(max = 150)
  private String occupation = "";

  @Size(max = 40)
  private String maritalStatus = "";

  @Size(max = 40)
  private String phone = "";

  @Size(max = 180)
  private String guardianName = "";

  @Size(max = 8)
  private String guardianDni = "";

  @Size(max = 40)
  private String guardianPhone = "";

  @Size(max = 10000)
  private String reason = "";

  @Size(max = 10000)
  private String symptoms = "";

  @Size(max = 180)
  private String illnessDuration = "";

  @Size(max = 10000)
  private String allergyDetails = "";

  @Size(max = 10000)
  private String otherHistory = "";

  @Size(max = 10000)
  private String clinicalExam = "";

  @Size(max = 10000)
  private String diagnosis = "";

  @Size(max = 10000)
  private String toothNotes = "";

  @Size(max = 180)
  private String blackLegend = "";

  @Size(max = 180)
  private String yellowLegend = "";

  @Size(max = 180)
  private String greenLegend = "";

  @Size(max = 30)
  private String consentStatus = "";

  @Size(max = 10000)
  private String consentDetails = "";

  @Size(max = 10000)
  private String revocation = "";

  @Size(max = 180)
  private String professional = "";

  @NotNull
  @Size(max = 9)
  private Map<String, String> conditions = new LinkedHashMap<>();

  @NotNull
  @Size(max = 260)
  private Map<String, String> teeth = new LinkedHashMap<>();

  @NotNull
  @Size(max = 200)
  @Valid
  private List<TreatmentInput> treatments = new ArrayList<>();

  @NotNull
  @Size(max = 500)
  @Valid
  private List<PaymentInput> payments = new ArrayList<>();

  public record TreatmentInput(
      @NotBlank @Size(max = 1000) String description,
      @NotNull @DecimalMin("0.00") @Digits(integer = 9, fraction = 2) BigDecimal amount) {}

  public record PaymentInput(
      Long id,
      @NotBlank String date,
      @NotNull @DecimalMin("0.01") @Digits(integer = 9, fraction = 2) BigDecimal amount,
      @Size(max = 180) String signer,
      @Size(max = 200) String receiptName,
      Boolean hasReceipt) {}

  public HistoryRequest() {}

  public long getVersion() {
    return this.version;
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

  public String getBirthDate() {
    return this.birthDate;
  }

  public String getConsentDate() {
    return this.consentDate;
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

  public List<TreatmentInput> getTreatments() {
    return this.treatments;
  }

  public List<PaymentInput> getPayments() {
    return this.payments;
  }

  public void setVersion(final long version) {
    this.version = version;
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

  public void setBirthDate(final String birthDate) {
    this.birthDate = birthDate;
  }

  public void setConsentDate(final String consentDate) {
    this.consentDate = consentDate;
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

  public void setTreatments(final List<TreatmentInput> treatments) {
    this.treatments = treatments;
  }

  public void setPayments(final List<PaymentInput> payments) {
    this.payments = payments;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof HistoryRequest)) return false;
    final HistoryRequest other = (HistoryRequest) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.getVersion() != other.getVersion()) return false;
    final java.lang.Object this$fullName = this.getFullName();
    final java.lang.Object other$fullName = other.getFullName();
    if (this$fullName == null ? other$fullName != null : !this$fullName.equals(other$fullName))
      return false;
    final java.lang.Object this$dni = this.getDni();
    final java.lang.Object other$dni = other.getDni();
    if (this$dni == null ? other$dni != null : !this$dni.equals(other$dni)) return false;
    final java.lang.Object this$sex = this.getSex();
    final java.lang.Object other$sex = other.getSex();
    if (this$sex == null ? other$sex != null : !this$sex.equals(other$sex)) return false;
    final java.lang.Object this$birthDate = this.getBirthDate();
    final java.lang.Object other$birthDate = other.getBirthDate();
    if (this$birthDate == null ? other$birthDate != null : !this$birthDate.equals(other$birthDate))
      return false;
    final java.lang.Object this$consentDate = this.getConsentDate();
    final java.lang.Object other$consentDate = other.getConsentDate();
    if (this$consentDate == null
        ? other$consentDate != null
        : !this$consentDate.equals(other$consentDate)) return false;
    final java.lang.Object this$address = this.getAddress();
    final java.lang.Object other$address = other.getAddress();
    if (this$address == null ? other$address != null : !this$address.equals(other$address))
      return false;
    final java.lang.Object this$origin = this.getOrigin();
    final java.lang.Object other$origin = other.getOrigin();
    if (this$origin == null ? other$origin != null : !this$origin.equals(other$origin))
      return false;
    final java.lang.Object this$occupation = this.getOccupation();
    final java.lang.Object other$occupation = other.getOccupation();
    if (this$occupation == null
        ? other$occupation != null
        : !this$occupation.equals(other$occupation)) return false;
    final java.lang.Object this$maritalStatus = this.getMaritalStatus();
    final java.lang.Object other$maritalStatus = other.getMaritalStatus();
    if (this$maritalStatus == null
        ? other$maritalStatus != null
        : !this$maritalStatus.equals(other$maritalStatus)) return false;
    final java.lang.Object this$phone = this.getPhone();
    final java.lang.Object other$phone = other.getPhone();
    if (this$phone == null ? other$phone != null : !this$phone.equals(other$phone)) return false;
    final java.lang.Object this$guardianName = this.getGuardianName();
    final java.lang.Object other$guardianName = other.getGuardianName();
    if (this$guardianName == null
        ? other$guardianName != null
        : !this$guardianName.equals(other$guardianName)) return false;
    final java.lang.Object this$guardianDni = this.getGuardianDni();
    final java.lang.Object other$guardianDni = other.getGuardianDni();
    if (this$guardianDni == null
        ? other$guardianDni != null
        : !this$guardianDni.equals(other$guardianDni)) return false;
    final java.lang.Object this$guardianPhone = this.getGuardianPhone();
    final java.lang.Object other$guardianPhone = other.getGuardianPhone();
    if (this$guardianPhone == null
        ? other$guardianPhone != null
        : !this$guardianPhone.equals(other$guardianPhone)) return false;
    final java.lang.Object this$reason = this.getReason();
    final java.lang.Object other$reason = other.getReason();
    if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason))
      return false;
    final java.lang.Object this$symptoms = this.getSymptoms();
    final java.lang.Object other$symptoms = other.getSymptoms();
    if (this$symptoms == null ? other$symptoms != null : !this$symptoms.equals(other$symptoms))
      return false;
    final java.lang.Object this$illnessDuration = this.getIllnessDuration();
    final java.lang.Object other$illnessDuration = other.getIllnessDuration();
    if (this$illnessDuration == null
        ? other$illnessDuration != null
        : !this$illnessDuration.equals(other$illnessDuration)) return false;
    final java.lang.Object this$allergyDetails = this.getAllergyDetails();
    final java.lang.Object other$allergyDetails = other.getAllergyDetails();
    if (this$allergyDetails == null
        ? other$allergyDetails != null
        : !this$allergyDetails.equals(other$allergyDetails)) return false;
    final java.lang.Object this$otherHistory = this.getOtherHistory();
    final java.lang.Object other$otherHistory = other.getOtherHistory();
    if (this$otherHistory == null
        ? other$otherHistory != null
        : !this$otherHistory.equals(other$otherHistory)) return false;
    final java.lang.Object this$clinicalExam = this.getClinicalExam();
    final java.lang.Object other$clinicalExam = other.getClinicalExam();
    if (this$clinicalExam == null
        ? other$clinicalExam != null
        : !this$clinicalExam.equals(other$clinicalExam)) return false;
    final java.lang.Object this$diagnosis = this.getDiagnosis();
    final java.lang.Object other$diagnosis = other.getDiagnosis();
    if (this$diagnosis == null ? other$diagnosis != null : !this$diagnosis.equals(other$diagnosis))
      return false;
    final java.lang.Object this$toothNotes = this.getToothNotes();
    final java.lang.Object other$toothNotes = other.getToothNotes();
    if (this$toothNotes == null
        ? other$toothNotes != null
        : !this$toothNotes.equals(other$toothNotes)) return false;
    final java.lang.Object this$blackLegend = this.getBlackLegend();
    final java.lang.Object other$blackLegend = other.getBlackLegend();
    if (this$blackLegend == null
        ? other$blackLegend != null
        : !this$blackLegend.equals(other$blackLegend)) return false;
    final java.lang.Object this$yellowLegend = this.getYellowLegend();
    final java.lang.Object other$yellowLegend = other.getYellowLegend();
    if (this$yellowLegend == null
        ? other$yellowLegend != null
        : !this$yellowLegend.equals(other$yellowLegend)) return false;
    final java.lang.Object this$greenLegend = this.getGreenLegend();
    final java.lang.Object other$greenLegend = other.getGreenLegend();
    if (this$greenLegend == null
        ? other$greenLegend != null
        : !this$greenLegend.equals(other$greenLegend)) return false;
    final java.lang.Object this$consentStatus = this.getConsentStatus();
    final java.lang.Object other$consentStatus = other.getConsentStatus();
    if (this$consentStatus == null
        ? other$consentStatus != null
        : !this$consentStatus.equals(other$consentStatus)) return false;
    final java.lang.Object this$consentDetails = this.getConsentDetails();
    final java.lang.Object other$consentDetails = other.getConsentDetails();
    if (this$consentDetails == null
        ? other$consentDetails != null
        : !this$consentDetails.equals(other$consentDetails)) return false;
    final java.lang.Object this$revocation = this.getRevocation();
    final java.lang.Object other$revocation = other.getRevocation();
    if (this$revocation == null
        ? other$revocation != null
        : !this$revocation.equals(other$revocation)) return false;
    final java.lang.Object this$professional = this.getProfessional();
    final java.lang.Object other$professional = other.getProfessional();
    if (this$professional == null
        ? other$professional != null
        : !this$professional.equals(other$professional)) return false;
    final java.lang.Object this$conditions = this.getConditions();
    final java.lang.Object other$conditions = other.getConditions();
    if (this$conditions == null
        ? other$conditions != null
        : !this$conditions.equals(other$conditions)) return false;
    final java.lang.Object this$teeth = this.getTeeth();
    final java.lang.Object other$teeth = other.getTeeth();
    if (this$teeth == null ? other$teeth != null : !this$teeth.equals(other$teeth)) return false;
    final java.lang.Object this$treatments = this.getTreatments();
    final java.lang.Object other$treatments = other.getTreatments();
    if (this$treatments == null
        ? other$treatments != null
        : !this$treatments.equals(other$treatments)) return false;
    final java.lang.Object this$payments = this.getPayments();
    final java.lang.Object other$payments = other.getPayments();
    if (this$payments == null ? other$payments != null : !this$payments.equals(other$payments))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof HistoryRequest;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $version = this.getVersion();
    result = result * PRIME + (int) ($version >>> 32 ^ $version);
    final java.lang.Object $fullName = this.getFullName();
    result = result * PRIME + ($fullName == null ? 43 : $fullName.hashCode());
    final java.lang.Object $dni = this.getDni();
    result = result * PRIME + ($dni == null ? 43 : $dni.hashCode());
    final java.lang.Object $sex = this.getSex();
    result = result * PRIME + ($sex == null ? 43 : $sex.hashCode());
    final java.lang.Object $birthDate = this.getBirthDate();
    result = result * PRIME + ($birthDate == null ? 43 : $birthDate.hashCode());
    final java.lang.Object $consentDate = this.getConsentDate();
    result = result * PRIME + ($consentDate == null ? 43 : $consentDate.hashCode());
    final java.lang.Object $address = this.getAddress();
    result = result * PRIME + ($address == null ? 43 : $address.hashCode());
    final java.lang.Object $origin = this.getOrigin();
    result = result * PRIME + ($origin == null ? 43 : $origin.hashCode());
    final java.lang.Object $occupation = this.getOccupation();
    result = result * PRIME + ($occupation == null ? 43 : $occupation.hashCode());
    final java.lang.Object $maritalStatus = this.getMaritalStatus();
    result = result * PRIME + ($maritalStatus == null ? 43 : $maritalStatus.hashCode());
    final java.lang.Object $phone = this.getPhone();
    result = result * PRIME + ($phone == null ? 43 : $phone.hashCode());
    final java.lang.Object $guardianName = this.getGuardianName();
    result = result * PRIME + ($guardianName == null ? 43 : $guardianName.hashCode());
    final java.lang.Object $guardianDni = this.getGuardianDni();
    result = result * PRIME + ($guardianDni == null ? 43 : $guardianDni.hashCode());
    final java.lang.Object $guardianPhone = this.getGuardianPhone();
    result = result * PRIME + ($guardianPhone == null ? 43 : $guardianPhone.hashCode());
    final java.lang.Object $reason = this.getReason();
    result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
    final java.lang.Object $symptoms = this.getSymptoms();
    result = result * PRIME + ($symptoms == null ? 43 : $symptoms.hashCode());
    final java.lang.Object $illnessDuration = this.getIllnessDuration();
    result = result * PRIME + ($illnessDuration == null ? 43 : $illnessDuration.hashCode());
    final java.lang.Object $allergyDetails = this.getAllergyDetails();
    result = result * PRIME + ($allergyDetails == null ? 43 : $allergyDetails.hashCode());
    final java.lang.Object $otherHistory = this.getOtherHistory();
    result = result * PRIME + ($otherHistory == null ? 43 : $otherHistory.hashCode());
    final java.lang.Object $clinicalExam = this.getClinicalExam();
    result = result * PRIME + ($clinicalExam == null ? 43 : $clinicalExam.hashCode());
    final java.lang.Object $diagnosis = this.getDiagnosis();
    result = result * PRIME + ($diagnosis == null ? 43 : $diagnosis.hashCode());
    final java.lang.Object $toothNotes = this.getToothNotes();
    result = result * PRIME + ($toothNotes == null ? 43 : $toothNotes.hashCode());
    final java.lang.Object $blackLegend = this.getBlackLegend();
    result = result * PRIME + ($blackLegend == null ? 43 : $blackLegend.hashCode());
    final java.lang.Object $yellowLegend = this.getYellowLegend();
    result = result * PRIME + ($yellowLegend == null ? 43 : $yellowLegend.hashCode());
    final java.lang.Object $greenLegend = this.getGreenLegend();
    result = result * PRIME + ($greenLegend == null ? 43 : $greenLegend.hashCode());
    final java.lang.Object $consentStatus = this.getConsentStatus();
    result = result * PRIME + ($consentStatus == null ? 43 : $consentStatus.hashCode());
    final java.lang.Object $consentDetails = this.getConsentDetails();
    result = result * PRIME + ($consentDetails == null ? 43 : $consentDetails.hashCode());
    final java.lang.Object $revocation = this.getRevocation();
    result = result * PRIME + ($revocation == null ? 43 : $revocation.hashCode());
    final java.lang.Object $professional = this.getProfessional();
    result = result * PRIME + ($professional == null ? 43 : $professional.hashCode());
    final java.lang.Object $conditions = this.getConditions();
    result = result * PRIME + ($conditions == null ? 43 : $conditions.hashCode());
    final java.lang.Object $teeth = this.getTeeth();
    result = result * PRIME + ($teeth == null ? 43 : $teeth.hashCode());
    final java.lang.Object $treatments = this.getTreatments();
    result = result * PRIME + ($treatments == null ? 43 : $treatments.hashCode());
    final java.lang.Object $payments = this.getPayments();
    result = result * PRIME + ($payments == null ? 43 : $payments.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "HistoryRequest(version="
        + this.getVersion()
        + ", fullName="
        + this.getFullName()
        + ", dni="
        + this.getDni()
        + ", sex="
        + this.getSex()
        + ", birthDate="
        + this.getBirthDate()
        + ", consentDate="
        + this.getConsentDate()
        + ", address="
        + this.getAddress()
        + ", origin="
        + this.getOrigin()
        + ", occupation="
        + this.getOccupation()
        + ", maritalStatus="
        + this.getMaritalStatus()
        + ", phone="
        + this.getPhone()
        + ", guardianName="
        + this.getGuardianName()
        + ", guardianDni="
        + this.getGuardianDni()
        + ", guardianPhone="
        + this.getGuardianPhone()
        + ", reason="
        + this.getReason()
        + ", symptoms="
        + this.getSymptoms()
        + ", illnessDuration="
        + this.getIllnessDuration()
        + ", allergyDetails="
        + this.getAllergyDetails()
        + ", otherHistory="
        + this.getOtherHistory()
        + ", clinicalExam="
        + this.getClinicalExam()
        + ", diagnosis="
        + this.getDiagnosis()
        + ", toothNotes="
        + this.getToothNotes()
        + ", blackLegend="
        + this.getBlackLegend()
        + ", yellowLegend="
        + this.getYellowLegend()
        + ", greenLegend="
        + this.getGreenLegend()
        + ", consentStatus="
        + this.getConsentStatus()
        + ", consentDetails="
        + this.getConsentDetails()
        + ", revocation="
        + this.getRevocation()
        + ", professional="
        + this.getProfessional()
        + ", conditions="
        + this.getConditions()
        + ", teeth="
        + this.getTeeth()
        + ", treatments="
        + this.getTreatments()
        + ", payments="
        + this.getPayments()
        + ")";
  }
}
