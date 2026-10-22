package pe.odontopoint.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.odontopoint.entity.PaymentReceipt;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {}
