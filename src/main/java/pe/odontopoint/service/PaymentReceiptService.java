package pe.odontopoint.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.odontopoint.dto.PaymentReceiptView;
import pe.odontopoint.entity.PaymentReceipt;
import pe.odontopoint.exception.BusinessException;
import pe.odontopoint.exception.NotFoundException;
import pe.odontopoint.repository.PaymentReceiptRepository;
import pe.odontopoint.repository.PaymentRepository;
import pe.odontopoint.util.ClinicalDates;

@Service
@Transactional(readOnly = true)
public class PaymentReceiptService {
  private final PaymentRepository payments;
  private final PaymentReceiptRepository receipts;
  private final AuditService audit;

  public PaymentReceipt find(Long paymentId) {
    var receipt = receipts.findById(paymentId).orElseThrow(NotFoundException::new);
    receipt.getContent();
    return receipt;
  }

  @Transactional
  public PaymentReceiptView upload(Long paymentId, MultipartFile file, String actor)
      throws IOException {
    var payment = payments.findById(paymentId).orElseThrow(NotFoundException::new);
    if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
      throw new BusinessException("Seleccione una captura PNG o JPG de hasta 5 MB.");
    byte[] bytes = file.getBytes();
    String format = validateImage(bytes);
    String name =
        file.getOriginalFilename() == null ? "comprobante-pago" : file.getOriginalFilename();
    name = name.replaceAll("[\\\\/\\r\\n]", "_");
    name = name.substring(0, Math.min(180, name.length()));
    var receipt = receipts.findById(paymentId).orElseGet(PaymentReceipt::new);
    receipt.setPayment(payment);
    receipt.setOriginalName(name);
    receipt.setMediaType(format.equals("png") ? "image/png" : "image/jpeg");
    receipt.setSha256(sha256(bytes));
    receipt.setUploadedAt(ClinicalDates.now());
    receipt.setUploadedBy(actor);
    receipt.setContent(bytes);
    receipts.saveAndFlush(receipt);
    // Se conserva el nombre en la columna histórica "comprobante" para facilitar
    // búsquedas/exportaciones.
    payment.setReceipt(name);
    payments.save(payment);
    audit.record(payment.getHistory().getId(), "COMPROBANTE DE PAGO ADJUNTADO", actor);
    return view(receipt);
  }

  @Transactional
  public void delete(Long paymentId, String actor) {
    var payment = payments.findById(paymentId).orElseThrow(NotFoundException::new);
    var receipt = receipts.findById(paymentId).orElseThrow(NotFoundException::new);
    receipts.delete(receipt);
    payment.setReceipt("");
    payments.save(payment);
    audit.record(payment.getHistory().getId(), "COMPROBANTE DE PAGO ELIMINADO", actor);
  }

  private String validateImage(byte[] bytes) {
    try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      var readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext()) throw new BusinessException("El archivo no es una imagen válida.");
      var reader = readers.next();
      try {
        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
        reader.setInput(stream);
        long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
        if (!Set.of("png", "jpeg", "jpg").contains(format) || pixels > 16000000)
          throw new BusinessException("Use PNG o JPG de hasta 16 megapíxeles.");
        if (reader.read(0) == null) throw new BusinessException("La captura está dañada.");
        return format;
      } finally {
        reader.dispose();
      }
    } catch (IOException ex) {
      throw new BusinessException("La captura está dañada o incompleta.");
    }
  }

  private String sha256(byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private PaymentReceiptView view(PaymentReceipt r) {
    return new PaymentReceiptView(
        r.getPayment().getId(),
        r.getOriginalName(),
        ClinicalDates.format(r.getUploadedAt()),
        r.getUploadedBy(),
        r.getSha256());
  }

  public PaymentReceiptService(
      final PaymentRepository payments,
      final PaymentReceiptRepository receipts,
      final AuditService audit) {
    this.payments = payments;
    this.receipts = receipts;
    this.audit = audit;
  }
}
