package ru.kubarev.releaseflow.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data public class RegistrationForm {
 @NotBlank(message="Введите логин") @Pattern(regexp="[a-zA-Z0-9._-]{3,40}",message="3–40 латинских букв, цифр или . _ -")
 private String username;
 @NotBlank(message="Введите имя") @Size(max=80) private String displayName;
 @NotBlank(message="Введите пароль") @Size(min=8,max=60,message="От 8 до 60 символов") private String password;
 @AssertTrue(message="Пароль должен занимать не более 72 байт UTF-8")
 public boolean isPasswordLengthValid(){return password==null||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=72;}
}
