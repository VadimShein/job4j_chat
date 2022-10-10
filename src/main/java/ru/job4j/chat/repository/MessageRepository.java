package ru.job4j.chat.repository;

import org.springframework.data.repository.CrudRepository;
import ru.job4j.chat.entity.Message;

import java.util.Optional;

public interface MessageRepository extends CrudRepository<Message, Long> {
    Optional<Message> findById(int id);
    Iterable<Message> findMessagesByRoomId(int id);
}
