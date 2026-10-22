package pe.odontopoint.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.odontopoint.dto.PaymentReceiptView;
import pe.odontopoint.service.PaymentReceiptService;

@RestController
public class PaymentReceiptController {
  private final PaymentReceiptService receipts;

  @PostMapping("/api/payments/{paymentId}/receipt")
  public ResponseEntity<PaymentReceiptView> upload(
      @PathVariable Long paymentId,
      @RequestParam("file") MultipartFile file,
      java.security.Principal actor)
      throws IOException {
    return ResponseEntity.status(201).body(receipts.upload(paymentId, file, actor.getName()));
  }

  @DeleteMapping("/api/payments/{paymentId}/receipt")
  public ResponseEntity<Void> delete(@PathVariable Long paymentId, java.security.Principal actor) {
    receipts.delete(paymentId, actor.getName());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/payments/{paymentId}/receipt")
  public ResponseEntity<byte[]> content(@PathVariable Long paymentId) {
    var receipt = receipts.find(paymentId);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(receipt.getMediaType()))
        .header(
            "Content-Disposition",
            ContentDisposition.inline()
                .filename(receipt.getOriginalName(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .cacheControl(CacheControl.noStore())
        .body(receipt.getContent());
  }

  public PaymentReceiptController(final PaymentReceiptService receipts) {
    this.receipts = receipts;
  }
}
