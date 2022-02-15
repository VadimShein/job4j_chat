package ru.job4j.chat.repository;

import org.springframework.data.repository.CrudRepository;
import ru.job4j.chat.entity.Role;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long>  {
    Optional<Role> findById(int id);
}
