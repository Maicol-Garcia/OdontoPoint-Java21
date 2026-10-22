package pe.odontopoint.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.odontopoint.entity.SequenceCounter;
import pe.odontopoint.repository.PatientHistoryRepository;
import pe.odontopoint.repository.SequenceCounterRepository;

@Component
public class DatabaseInitializer implements CommandLineRunner {
  private final SequenceCounterRepository counters;
  private final PatientHistoryRepository histories;

  @Transactional
  public void run(String... args) {
    ensureCounter("historia", histories.maxId());
    ensureCounter("configuracion", 0L);
    ensureCounter("agenda", 0L);
  }

  private void ensureCounter(String key, long initialValue) {
    if (!counters.existsById(key)) {
      var counter = new SequenceCounter();
      counter.setCounterKey(key);
      counter.setNextValue(initialValue);
      counters.save(counter);
    }
  }

  public DatabaseInitializer(
      final SequenceCounterRepository counters, final PatientHistoryRepository histories) {
    this.counters = counters;
    this.histories = histories;
  }
}
