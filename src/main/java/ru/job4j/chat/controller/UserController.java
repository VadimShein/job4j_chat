package ru.job4j.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.entity.Operation;
import ru.job4j.chat.entity.Person;
import ru.job4j.chat.service.UserService;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService users;
    private final BCryptPasswordEncoder encoder;

    public UserController(UserService users, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostMapping("/sign-up")
    @Validated(Operation.OnCreate.class)
    public void signUp(@Valid @RequestBody Person person) {
        if (person.getUsername() == null || person.getPassword() == null || person.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "Username, password and role mustn't be empty");
        }
        person.setPassword(encoder.encode(person.getPassword()));
        users.save(person);
    }

    @GetMapping("/all")
    public Iterable<Person> findAll() {
        return users.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable String id) {
        return new ResponseEntity<>(users.findById(Integer.parseInt(id)).orElse(new Person()), HttpStatus.OK);
    }

    @PatchMapping("/")
    public ResponseEntity<Person> patch(@Valid @RequestBody Person person) throws InvocationTargetException, IllegalAccessException {
        return new ResponseEntity<>(this.users.patch(person), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        users.delete(Integer.parseInt(id));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}