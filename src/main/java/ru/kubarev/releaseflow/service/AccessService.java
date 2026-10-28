package ru.kubarev.releaseflow.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import ru.kubarev.releaseflow.domain.AppUser;
@Service @RequiredArgsConstructor public class AccessService {
 private final UserService users;
 public boolean ownOnly(){AppUser u=users.current();return !u.isAdmin()&&u.canWrite();}
 public void read(AppUser owner){if(ownOnly()&&!owner.getId().equals(users.current().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 public void write(AppUser owner){AppUser u=users.current();if(!u.canWrite()||(!u.isAdmin()&&!owner.getId().equals(u.getId())))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 public void create(){if(!users.current().canWrite())throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
}
