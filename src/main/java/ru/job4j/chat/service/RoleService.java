package ru.job4j.chat.service;

import org.springframework.stereotype.Service;
import ru.job4j.chat.entity.Role;
import ru.job4j.chat.repository.RoleRepository;

import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roles;

    public RoleService(RoleRepository roles) {
        this.roles = roles;
    }

    public Iterable<Role> findAll() {
        return roles.findAll();
    }

    public Optional<Role> findById(int id) {
        return roles.findById(id);
    }

    public Role save(Role role) {
        return roles.save(role);
    }

    public void delete(Role role) {
        roles.delete(role);
    }
}
