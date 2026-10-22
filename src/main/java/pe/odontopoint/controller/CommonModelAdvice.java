package pe.odontopoint.controller;

import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class CommonModelAdvice {
  @ModelAttribute("username")
  public String username(java.security.Principal principal) {
    return principal == null ? "" : principal.getName();
  }
}
