package ru.kubarev.releaseflow;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import ru.kubarev.releaseflow.domain.*;
import ru.kubarev.releaseflow.repository.*;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:course;DB_CLOSE_DELAY=-1",
      "logging.file.name=target/test-app.log"
    })
@AutoConfigureMockMvc
@Transactional
class AccessIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired ReleaseRepository releases;
  @Autowired TaskRepository tasks;
  @Autowired ActionRepository actions;
  @Autowired PasswordEncoder encoder;
  AppUser admin, egor, anna, viewer;
  Release own, other;
  Task ownTask, otherTask;

  @BeforeEach
  void setup() {
    admin = user("admin", Role.ADMIN);
    egor = user("egor", Role.USER);
    anna = user("anna", Role.USER);
    viewer = user("viewer", Role.READ_ONLY);
    own = release("OWN-RELEASE", egor);
    other = release("FOREIGN-RELEASE", anna);
    ownTask = task("OWN-TASK", own, egor);
    otherTask = task("FOREIGN-TASK", other, anna);
  }

  AppUser user(String n, Role role) {
    AppUser u = new AppUser();
    u.setUsername(n);
    u.setDisplayName(n);
    u.setPassword(encoder.encode("DemoPass!2026"));
    u.setRoles(EnumSet.of(Role.READ_ONLY, role));
    return users.saveAndFlush(u);
  }

  Release release(String n, AppUser u) {
    Release r = new Release();
    r.setName(n);
    r.setDescription("example");
    r.setTargetDate(LocalDate.of(2026, 12, 1));
    r.setStatus(ReleaseStatus.ACTIVE);
    r.setOwner(u);
    return releases.saveAndFlush(r);
  }

  Task task(String n, Release r, AppUser u) {
    Task t = new Task();
    t.setTitle(n);
    t.setDescription("desc");
    t.setDays(new BigDecimal("2.5"));
    t.setDailyRate(new BigDecimal("10000"));
    t.setDueDate(LocalDate.of(2026, 11, 15));
    t.setOwner(u);
    t.setRelease(r);
    return tasks.saveAndFlush(t);
  }

  LinkedMultiValueMap<String, String> form(Task t) {
    var f = new LinkedMultiValueMap<String, String>();
    f.add("title", "Updated");
    f.add("description", "Updated description");
    f.add("days", "2.50");
    f.add("dailyRate", "12000.00");
    f.add("dueDate", "2026-11-15");
    f.add("status", "IN_PROGRESS");
    f.add("priority", "HIGH");
    f.add("releaseId", t.getRelease().getId().toString());
    f.add("version", t.getVersion().toString());
    return f;
  }

  @ParameterizedTest
  @ValueSource(strings = {"/", "/about", "/login", "/register", "/assets/app.css"})
  void publicPages(String p) throws Exception {
    mvc.perform(get(p)).andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/dashboard",
        "/tasks",
        "/releases",
        "/logs",
        "/estimate",
        "/admin/users",
        "/actuator/health"
      })
  void guestRedirect(String p) throws Exception {
    mvc.perform(get(p))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  void registerOnlyReadOnly() throws Exception {
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("username", "NewUser")
                .param("displayName", "New User")
                .param("password", "NewDemo!2026")
                .param("roles", "ADMIN"))
        .andExpect(redirectedUrl("/login?registered"));
    var u = users.findByUsername("newuser").orElseThrow();
    assertEquals(Set.of(Role.READ_ONLY), u.getRoles());
    assertTrue(encoder.matches("NewDemo!2026", u.getPassword()));
  }

  @Test
  void duplicateRegistration() throws Exception {
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("username", "EGOR")
                .param("displayName", "X")
                .param("password", "NewDemo!2026"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "username"));
    assertEquals(4, users.count());
  }

  @Test
  void invalidRegistration() throws Exception {
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("username", "!")
                .param("displayName", "")
                .param("password", "123"))
        .andExpect(status().isOk())
        .andExpect(model().hasErrors());
    assertEquals(4, users.count());
  }

  @Test
  void passwordByteLimit() throws Exception {
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("username", "longpass")
                .param("displayName", "X")
                .param("password", "я".repeat(40)))
        .andExpect(status().isOk())
        .andExpect(model().hasErrors());
    assertTrue(users.findByUsername("longpass").isEmpty());
  }

  @Test
  void loginAndBadPassword() throws Exception {
    mvc.perform(formLogin().user("egor").password("DemoPass!2026"))
        .andExpect(authenticated())
        .andExpect(redirectedUrl("/dashboard"));
    mvc.perform(formLogin().user("egor").password("bad")).andExpect(unauthenticated());
    assertTrue(actions.findAll().stream().anyMatch(a -> a.getAction().equals("LOGIN")));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/register", "/login", "/tasks", "/releases", "/estimate"})
  void csrfRequired(String p) throws Exception {
    mvc.perform(post(p)).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "viewer", roles = "READ_ONLY")
  void readerSeesAllButCannotWrite() throws Exception {
    String html =
        mvc.perform(get("/tasks"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertTrue(html.contains("OWN-TASK"));
    assertTrue(html.contains("FOREIGN-TASK"));
    mvc.perform(get("/tasks/new")).andExpect(status().isForbidden());
    mvc.perform(post("/tasks").with(csrf()).params(form(ownTask)))
        .andExpect(status().isForbidden());
    mvc.perform(post("/tasks/" + ownTask.getId() + "/delete").with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void userListsOnlyOwn() throws Exception {
    for (String url : List.of("/tasks", "/board", "/dashboard")) {
      String html =
          mvc.perform(get(url))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      assertTrue(html.contains("OWN-TASK"));
      assertFalse(html.contains("FOREIGN-TASK"));
    }
    String html = mvc.perform(get("/releases")).andReturn().getResponse().getContentAsString();
    assertFalse(html.contains("FOREIGN-RELEASE"));
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void foreignReadWriteDeleteDenied() throws Exception {
    mvc.perform(get("/tasks/" + otherTask.getId() + "/edit")).andExpect(status().isForbidden());
    mvc.perform(post("/tasks/" + otherTask.getId()).with(csrf()).params(form(otherTask)))
        .andExpect(status().isForbidden());
    mvc.perform(post("/tasks/" + otherTask.getId() + "/delete").with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/releases/" + other.getId() + "/edit")).andExpect(status().isForbidden());
    mvc.perform(post("/releases/" + other.getId() + "/delete").with(csrf()))
        .andExpect(status().isForbidden());
    assertTrue(tasks.existsById(otherTask.getId()));
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void foreignReleaseBindingDenied() throws Exception {
    var f = form(ownTask);
    f.set("releaseId", other.getId().toString());
    mvc.perform(post("/tasks").with(csrf()).params(f)).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void taskCrudOwnershipAndAudit() throws Exception {
    var f = form(ownTask);
    f.remove("version");
    f.add("ownerId", anna.getId().toString());
    mvc.perform(post("/tasks").with(csrf()).params(f)).andExpect(redirectedUrl("/tasks"));
    var t = tasks.findAllByOrderByIdDesc().get(0);
    assertEquals(egor.getId(), t.getOwner().getId());
    mvc.perform(post("/tasks/" + t.getId()).with(csrf()).params(form(t)))
        .andExpect(redirectedUrl("/tasks"));
    mvc.perform(post("/tasks/" + t.getId() + "/delete").with(csrf()))
        .andExpect(redirectedUrl("/tasks"));
    assertFalse(tasks.existsById(t.getId()));
    assertTrue(actions.findAll().stream().anyMatch(a -> a.getAction().equals("TASK_DELETE")));
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void releaseCrudAndReferencedDelete() throws Exception {
    mvc.perform(
            post("/releases")
                .with(csrf())
                .param("name", "New Release")
                .param("description", "d")
                .param("targetDate", "2026-12-31")
                .param("status", "PLANNED"))
        .andExpect(redirectedUrl("/releases"));
    var r =
        releases.findAll().stream()
            .filter(x -> x.getName().equals("New Release"))
            .findFirst()
            .orElseThrow();
    mvc.perform(
            post("/releases/" + r.getId())
                .with(csrf())
                .param("version", r.getVersion().toString())
                .param("name", "Renamed")
                .param("description", "d")
                .param("targetDate", "2026-12-31")
                .param("status", "ACTIVE"))
        .andExpect(redirectedUrl("/releases"));
    mvc.perform(post("/releases/" + r.getId() + "/delete").with(csrf()))
        .andExpect(redirectedUrl("/releases"));
    assertFalse(releases.existsById(r.getId()));
    mvc.perform(post("/releases/" + own.getId() + "/delete").with(csrf()))
        .andExpect(flash().attributeExists("error"));
    assertTrue(releases.existsById(own.getId()));
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void staleVersionAndInvalidData() throws Exception {
    var f = form(ownTask);
    f.set("version", "999");
    mvc.perform(post("/tasks/" + ownTask.getId()).with(csrf()).params(f))
        .andExpect(status().isConflict());
    f = form(ownTask);
    f.set("days", "-1");
    f.set("title", "");
    mvc.perform(post("/tasks").with(csrf()).params(f))
        .andExpect(model().attributeHasFieldErrors("form", "days", "title"));
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void searchAndLogScope() throws Exception {
    String html =
        mvc.perform(get("/tasks").param("q", "not-existing"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertFalse(html.contains("OWN-TASK"));
    UserAction a = new UserAction();
    a.setActor("anna");
    a.setAction("TEST");
    a.setDateActions(Instant.now());
    a.setDescription("FOREIGN-LOG");
    actions.saveAndFlush(a);
    html = mvc.perform(get("/logs")).andReturn().getResponse().getContentAsString();
    assertFalse(html.contains("FOREIGN-LOG"));
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void adminDeniedAndEstimateForeignDenied() throws Exception {
    mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
    mvc.perform(get("/actuator/health")).andExpect(status().isForbidden());
    mvc.perform(
            post("/estimate")
                .with(csrf())
                .param("releaseId", other.getId().toString())
                .param("people", "1")
                .param("reserve", "15")
                .param("startDate", "2026-10-28"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminAllAndRoleChange() throws Exception {
    String html = mvc.perform(get("/tasks")).andReturn().getResponse().getContentAsString();
    assertTrue(html.contains("FOREIGN-TASK"));
    mvc.perform(post("/admin/users/" + viewer.getId() + "/role").with(csrf()).param("role", "USER"))
        .andExpect(redirectedUrl("/admin/users"));
    assertTrue(users.findById(viewer.getId()).orElseThrow().canWrite());
    mvc.perform(
            post("/admin/users/" + admin.getId() + "/role").with(csrf()).param("role", "READ_ONLY"))
        .andExpect(flash().attributeExists("error"));
    assertTrue(users.findById(admin.getId()).orElseThrow().isAdmin());
  }

  @Test
  @WithMockUser(username = "egor", roles = "ADMIN")
  void staleAdminAuthorityDoesNotGrantAccess() throws Exception {
    mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
    mvc.perform(get("/actuator/health")).andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(strings = {"health", "info", "metrics", "metrics/jvm.memory.used", "mappings"})
  @WithMockUser(username = "admin", roles = "ADMIN")
  void actuatorAvailableForAdmin(String endpoint) throws Exception {
    mvc.perform(get("/actuator/" + endpoint)).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void allFormsRenderAndNoHashLeak() throws Exception {
    for (String path :
        List.of("/tasks/new", "/releases/new", "/estimate", "/admin/users", "/about")) {
      String html =
          mvc.perform(get(path))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      assertFalse(html.contains(admin.getPassword()));
    }
  }

  @Test
  @WithMockUser(username = "egor", roles = "USER")
  void missingRecord404AndLogout() throws Exception {
    mvc.perform(get("/tasks/999999/edit")).andExpect(status().isNotFound());
    mvc.perform(logout()).andExpect(unauthenticated()).andExpect(redirectedUrl("/login?logout"));
  }
}
