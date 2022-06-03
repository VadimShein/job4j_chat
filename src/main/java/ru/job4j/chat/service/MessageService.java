package ru.job4j.chat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.entity.Message;
import ru.job4j.chat.entity.Person;
import ru.job4j.chat.repository.MessageRepository;
import ru.job4j.chat.repository.PersonRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;

@Service
public class MessageService {
    private final MessageRepository messages;
    private final PersonRepository persons;

    public MessageService(MessageRepository messages, PersonRepository persons) {
        this.messages = messages;
        this.persons = persons;
    }

    public Iterable<Message> findAll() {
        return messages.findAll();
    }

    public Optional<Message> findById(int id) {
        return messages.findById(id);
    }

    public Message save(Message message) {
        if (persons.findById(message.getAuthor().getId()).isPresent()) {
            message.setAuthor(persons.findById(message.getAuthor().getId()).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author is not found");
        }
        return messages.save(message);
    }

    public Message patch(Message message) throws InvocationTargetException, IllegalAccessException {
        Optional<Message> current = messages.findById(message.getId());
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
                var newValue = getMethod.invoke(message);
                if (newValue != null) {
                    setMethod.invoke(current.get(), newValue);
                }
            }
        }
        return messages.save(current.get());
    }

    public void delete(Message message) {
        messages.delete(message);
    }
}
