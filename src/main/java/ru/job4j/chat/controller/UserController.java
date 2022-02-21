package ru.job4j.chat.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.job4j.chat.entity.Person;
import ru.job4j.chat.repository.UserStore;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserStore users;
    private BCryptPasswordEncoder encoder;

    public UserController(UserStore users, BCryptPasswordEncoder encoder) {
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
    public List<Person> findAll() {
        return users.findAll();
    }

    @GetMapping("/{name}")
    public Person findById(@PathVariable String name) {
        return users.findByUsername(name);
    }
}