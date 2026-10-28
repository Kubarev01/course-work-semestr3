package ru.kubarev.releaseflow.config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.*;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import ru.kubarev.releaseflow.service.AuditService;
@Component @RequiredArgsConstructor public class SecurityAuditListener {
 private final AuditService audit;
 @EventListener public void login(AuthenticationSuccessEvent e){audit.record(e.getAuthentication().getName(),"LOGIN","Успешный вход");}
 @EventListener public void failure(AbstractAuthenticationFailureEvent e){audit.record(e.getAuthentication().getName(),"LOGIN_FAILURE","Неуспешный вход");}
 @EventListener public void logout(LogoutSuccessEvent e){audit.record(e.getAuthentication().getName(),"LOGOUT","Завершение сеанса");}
}
