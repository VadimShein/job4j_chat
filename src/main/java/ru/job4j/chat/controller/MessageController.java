package ru.job4j.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.entity.Message;
import ru.job4j.chat.service.MessageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/message")
public class MessageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class.getSimpleName());
    private final MessageService messages;
    private final ObjectMapper objectMapper;

    public MessageController(final MessageService messages, ObjectMapper objectMapper) {
        this.messages = messages;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public List<Message> findAll() {
        return StreamSupport.stream(
                this.messages.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> findById(@PathVariable int id) {
        var person = this.messages.findById(id);
        return new ResponseEntity<>(
                person.orElse(new Message()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PatchMapping("/")
    public ResponseEntity<Message> patch(@RequestBody Message message) throws InvocationTargetException, IllegalAccessException {
        Optional<Message> current = messages.findById(message.getId());
        if (current.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var methods = current.get().getClass().getDeclaredMethods();
        var namePerMethod = new HashMap<String, Method>();
        for (var method: methods) {
            var name = method.getName();
            if (name.startsWith("get") || name.startsWith("set")) {
                System.out.println(name + " || " + method);
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
                    setMethod.invoke(current, newValue);
                }
            }
        }
        return new ResponseEntity<>(this.messages.save(message), HttpStatus.CREATED);
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        if (message.getText() == null) {
            throw new NullPointerException("Message text mustn't be empty");
        }
        if (message.getText().contains("rudeWord")) {
            throw new IllegalArgumentException("Message text does not match the rules");
        }
        return new ResponseEntity<>(this.messages.save(message), HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Message message) {
        if (message.getText() == null) {
            throw new NullPointerException("Message text mustn't be empty");
        }
        this.messages.save(message);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Message message = new Message();
        message.setId(id);
        this.messages.delete(message);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public void exceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("message", e.getMessage());
            put("type", e.getClass());
        }}));
        LOGGER.error(e.getLocalizedMessage());
    }
}
