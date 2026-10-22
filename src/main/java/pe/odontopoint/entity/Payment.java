package pe.odontopoint.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pagos", indexes = @Index(name = "idx_pagos_fecha_historia", columnList = "fecha_pago,id_historia"))
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_historia", nullable = false)
  private PatientHistory history;

  @Column(name = "fecha_pago", nullable = false)
  private java.time.LocalDate paymentDate;

  @Column(name = "importe", nullable = false, precision = 12, scale = 2)
  private java.math.BigDecimal amount;

  @Column(name = "comprobante", length = 180)
  private String receipt;

  @Column(name = "firmante", length = 180)
  private String signer;

  @OneToOne(
      mappedBy = "payment",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private PaymentReceipt receiptFile;

  public Long getId() {
    return this.id;
  }

  public PatientHistory getHistory() {
    return this.history;
  }

  public java.time.LocalDate getPaymentDate() {
    return this.paymentDate;
  }

  public java.math.BigDecimal getAmount() {
    return this.amount;
  }

  public String getReceipt() {
    return this.receipt;
  }

  public String getSigner() {
    return this.signer;
  }

  public PaymentReceipt getReceiptFile() {
    return this.receiptFile;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public void setHistory(final PatientHistory history) {
    this.history = history;
  }

  public void setPaymentDate(final java.time.LocalDate paymentDate) {
    this.paymentDate = paymentDate;
  }

  public void setAmount(final java.math.BigDecimal amount) {
    this.amount = amount;
  }

  public void setReceipt(final String receipt) {
    this.receipt = receipt;
  }

  public void setSigner(final String signer) {
    this.signer = signer;
  }

  public void setReceiptFile(final PaymentReceipt receiptFile) {
    this.receiptFile = receiptFile;
  }
}
