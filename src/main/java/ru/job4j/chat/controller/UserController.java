package ru.job4j.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.job4j.chat.entity.Person;
import ru.job4j.chat.service.PersonService;

import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping("/users")
public class UserController {
    private PersonService users;
    private BCryptPasswordEncoder encoder;

    public UserController(PersonService users, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostMapping("/sign-up")
    public void signUp(@RequestBody Person person) {
        if (person.getUsername() == null || person.getPassword() == null || person.getRole() == null) {
            throw new NullPointerException("Username, password and role mustn't be empty");
        }
        person.setPassword(encoder.encode(person.getPassword()));
        users.save(person);
    }

    @GetMapping("/all")
    public Iterable<Person> findAll() {
        return users.findAll();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Person> findById(@PathVariable String name) {
        return new ResponseEntity<>(users.findByUsername(name).orElse(new Person()), HttpStatus.OK);
    }

    @PatchMapping("/")
    public ResponseEntity<Person> patch(@RequestBody Person person) throws InvocationTargetException, IllegalAccessException {
        return new ResponseEntity<>(this.users.patch(person), HttpStatus.CREATED);
    }
}