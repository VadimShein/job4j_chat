package ru.job4j.chat.service;

import org.springframework.stereotype.Service;
import ru.job4j.chat.entity.Message;
import ru.job4j.chat.repository.MessageRepository;


import java.util.Optional;

@Service
public class MessageService {
    private final MessageRepository messages;

    public MessageService(MessageRepository messages) {
        this.messages = messages;
    }

    public Iterable<Message> findAll() {
        return messages.findAll();
    }

    public Optional<Message> findById(int id) {
        return messages.findById(id);
    }

    public Message save(Message message) {
        return messages.save(message);
    }

    public void delete(Message message) {
        messages.delete(message);
    }
}
