package ru.job4j.chat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.entity.Person;
import ru.job4j.chat.repository.UserRepository;
import ru.job4j.chat.repository.RoleRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;
    private final RoleRepository roles;

    public UserService(UserRepository users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    public Iterable<Person> findAll() {
        return users.findAll();
    }

    public Optional<Person> findById(int id) {
        return users.findById(id);
    }

    public Optional<Person> findByUsername(String name) {
        return users.findByUsername(name);
    }

    @Transactional
    public Person save(Person person) {
        Person createdPerson;
        if (roles.findByRoleName(person.getRole().getRoleName()).isPresent()) {
            person.setRole(roles.findByRoleName(person.getRole().getRoleName()).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role is not found");
        }
        if (users.findByUsername(person.getUsername()).isEmpty()) {
            createdPerson = users.save(person);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Username is already taken");
        }
        return createdPerson;
    }

    public Person patch(Person person) throws InvocationTargetException, IllegalAccessException {
        Optional<Person> current = users.findById(person.getId());
        if (current.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var methods = current.get().getClass().getDeclaredMethods();
        var namePerMethod = new HashMap<String, Method>();
        for (var method: methods) {
            var name = method.getName();
            if (name.startsWith("get") || name.startsWith("set")) {
                namePerMethod.put(name, method);
            }
        }
        for (var name : namePerMethod.keySet()) {
            if (name.startsWith("get")) {
                var getMethod = namePerMethod.get(name);
                var setMethod = namePerMethod.get(name.replace("get", "set"));
                if (setMethod == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid properties mapping");
                }
                var newValue = getMethod.invoke(person);
                if (newValue != null) {
                    setMethod.invoke(current.get(), newValue);
                }
            }
        }
        return users.save(current.get());
    }

    public void delete(int id) {
        if (users.findById(id).isPresent()) {
            Person person = new Person();
            person.setId(id);
            users.delete(person);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User id is not found");
        }
    }
}
