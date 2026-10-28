package ru.kubarev.releaseflow.controller;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.dao.DataIntegrityViolationException;
import ru.kubarev.releaseflow.dto.RegistrationForm;
import ru.kubarev.releaseflow.service.UserService;
@Controller @RequiredArgsConstructor public class AuthController {
 private final UserService users;
 @GetMapping("/") public String home(){return "home";}
 @GetMapping("/about") public String about(){return "about";}
 @GetMapping("/login") public String login(){return "login";}
 @GetMapping("/register") public String register(Model m){m.addAttribute("form",new RegistrationForm());return "register";}
 @PostMapping("/register") public String register(@Valid @ModelAttribute("form")RegistrationForm form,BindingResult errors){
  if(errors.hasErrors()){form.setPassword(null);return "register";}
  try{users.register(form);}catch(IllegalArgumentException|DataIntegrityViolationException e){errors.rejectValue("username","duplicate","Этот логин уже занят");form.setPassword(null);return "register";}
  return "redirect:/login?registered";
 }
 @GetMapping("/forbidden") @ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
 public String forbidden(){return "forbidden";}
}
