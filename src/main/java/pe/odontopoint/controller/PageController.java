package pe.odontopoint.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.odontopoint.service.*;

@Controller
public class PageController {
  private final HistoryService histories;

  @GetMapping("/agenda")
  public String agenda() {
    return "agenda";
  }

  @GetMapping("/ingresos")
  public String ingresos() {
    return "ingresos";
  }

  @GetMapping("/catalogo")
  public String catalogo() {
    return "catalogo";
  }

  @GetMapping({"/", "/patients"})
  public String index(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "0") int page,
      Model model) {
    model.addAttribute("patients", histories.search(q, page));
    model.addAttribute("q", q);
    model.addAttribute("total", histories.count());
    model.addAttribute("month", histories.thisMonth());
    return "dashboard";
  }

  @GetMapping("/patients/new")
  public String create(Model model) {
    model.addAttribute("historyId", "");
    return "history";
  }

  @GetMapping("/patients/{id}")
  public String edit(@PathVariable Long id, Model model) {
    histories.find(id);
    model.addAttribute("historyId", id);
    return "history";
  }

  public PageController(final HistoryService histories) {
    this.histories = histories;
  }
}
