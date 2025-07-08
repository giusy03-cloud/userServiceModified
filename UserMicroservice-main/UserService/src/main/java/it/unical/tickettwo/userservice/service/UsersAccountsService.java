package it.unical.tickettwo.userservice.service;

import it.unical.tickettwo.userservice.domain.UsersAccounts;
import it.unical.tickettwo.userservice.repository.UsersAccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class UsersAccountsService {

    @Autowired
    private UsersAccountsRepository usersAccountsRepository;
    @Autowired
    private RestTemplate restTemplate;

    private final String BOOKING_SERVICE_URL = "http://localhost:8083/api/bookings/user/";
    private final String REVIEW_SERVICE_URL = "http://localhost:8082/reviews/user/";


    public List<UsersAccounts> getAllUsers() {
        return usersAccountsRepository.findAll();
    }

    public Optional<UsersAccounts> getUserById(Long id) {
        return usersAccountsRepository.findById(id);
    }

    // PasswordEncoder passato come parametro per evitare il ciclo
    public UsersAccounts registerUser(UsersAccounts user, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        System.out.println("Password originale: " + user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("Password codificata: " + user.getPassword());
        return usersAccountsRepository.save(user);
    }


    public void deleteUser(Long id, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(BOOKING_SERVICE_URL + id, HttpMethod.DELETE, entity, Void.class);
            System.out.println("Prenotazioni cancellate per userId " + id);
        } catch (Exception e) {
            System.err.println("Errore cancellazione prenotazioni: " + e.getMessage());
        }

        try {
            restTemplate.exchange(REVIEW_SERVICE_URL + id, HttpMethod.DELETE, entity, Void.class);
            System.out.println("Recensioni cancellate per userId " + id);
        } catch (Exception e) {
            System.err.println("Errore cancellazione recensioni: " + e.getMessage());
        }

        usersAccountsRepository.deleteById(id);
        System.out.println("Utente cancellato con ID: " + id);
    }




    public UsersAccounts getUserByUsername(String username) {
        return usersAccountsRepository.findByUsername(username);
    }
}
