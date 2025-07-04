package it.unical.tickettwo.userservice.controller;

import it.unical.tickettwo.userservice.domain.UsersAccounts;
import it.unical.tickettwo.userservice.service.UsersAccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UsersAccountsController {

    @Autowired
    private UsersAccountsService usersAccountsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UsersAccounts> getAllUsers() {
        return usersAccountsService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsersAccounts> getUserById(@PathVariable Long id) {
        Optional<UsersAccounts> user = usersAccountsService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping
    public UsersAccounts createUser(@RequestBody UsersAccounts user) {
        // Forza ruolo partecipante (o ruolo di default)

        return usersAccountsService.registerUser(user, passwordEncoder);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        usersAccountsService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UsersAccounts> getUserByUsername(@PathVariable String username) {
        UsersAccounts user = usersAccountsService.getUserByUsername(username);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UsersAccounts updatedUser, Authentication auth) {
        String username = auth.getName();
        UsersAccounts currentUser = usersAccountsService.getUserByUsername(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        }

        // Solo admin o l'utente stesso possono modificare
        if (!currentUser.getRole().equals("ADMIN") && currentUser.getId() != id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non puoi modificare il profilo di un altro utente");
        }

        // Procedi con l'aggiornamento (esempio semplificato)
        updatedUser.setId(id);
        // Assicurati di gestire password correttamente: se viene modificata, devi fare encode
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        } else {
            // Se non aggiorni password, mantieni quella vecchia
            updatedUser.setPassword(currentUser.getPassword());
        }

        UsersAccounts savedUser = usersAccountsService.registerUser(updatedUser, passwordEncoder);
        return ResponseEntity.ok(savedUser);
    }

}
