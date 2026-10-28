package ru.kubarev.releaseflow.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubarev.releaseflow.domain.UserAction;
import ru.kubarev.releaseflow.repository.ActionRepository;

@Service
@RequiredArgsConstructor
public class AuditService {
  private static final Logger log = LoggerFactory.getLogger(AuditService.class);
  private final ActionRepository repository;

  @Transactional
  public void record(String actor, String action, String description) {
    UserAction e = new UserAction();
    e.setDateActions(Instant.now());
    String safe = actor == null ? "anonymous" : actor.replaceAll("[\\r\\n]", "_");
    e.setActor(safe.substring(0, Math.min(80, safe.length())));
    e.setAction(action);
    e.setDescription(description);
    repository.save(e);
    log.info("AUDIT actor={} action={} detail={}", e.getActor(), action, description);
  }
}
