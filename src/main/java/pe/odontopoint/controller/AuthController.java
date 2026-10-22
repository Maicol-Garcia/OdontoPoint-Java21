package pe.odontopoint.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.odontopoint.dto.SetupRequest;
import pe.odontopoint.exception.BusinessException;
import pe.odontopoint.service.AccountService;

@Controller
public class AuthController {
  private final AccountService accounts;

  @GetMapping("/login")
  public String login() {
    return accounts.needsSetup() ? "redirect:/setup" : "login";
  }

  @GetMapping("/setup")
  public String setup(Model model) {
    if (!accounts.needsSetup()) return "redirect:/login";
    model.addAttribute("form", new SetupRequest());
    return "setup";
  }

  @PostMapping("/setup")
  public String setup(
      @Valid @ModelAttribute("form") SetupRequest form, BindingResult errors, Model model) {
    if (!accounts.needsSetup()) return "redirect:/login";
    if (errors.hasErrors()) {
      model.addAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
      return "setup";
    }
    try {
      accounts.setup(form);
      return "redirect:/login?created";
    } catch (BusinessException e) {
      model.addAttribute("message", e.getMessage());
      return "setup";
    }
  }

  @GetMapping("/account")
  public String account() {
    return "account";
  }

  @PostMapping("/account/password")
  public String change(
      @RequestParam String currentPassword,
      @RequestParam String newPassword,
      @RequestParam String confirmPassword,
      java.security.Principal principal,
      RedirectAttributes redirect) {
    try {
      accounts.changePassword(principal.getName(), currentPassword, newPassword, confirmPassword);
      redirect.addFlashAttribute("success", "Contraseña actualizada.");
    } catch (BusinessException e) {
      redirect.addFlashAttribute("message", e.getMessage());
    }
    return "redirect:/account";
  }

  public AuthController(final AccountService accounts) {
    this.accounts = accounts;
  }
}
