package ru.job4j.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.entity.Message;
import ru.job4j.chat.entity.Operation;
import ru.job4j.chat.service.MessageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
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

    @GetMapping("/roomId/{id}")
    public List<Message> findMessagesByRoomId(@PathVariable int id) {
        return StreamSupport.stream(
                this.messages.findMessagesByRoomId(id).spliterator(), false
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
    public ResponseEntity<Message> patch(@Valid @RequestBody Message message) throws InvocationTargetException, IllegalAccessException {
        if (message.getId() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Missed param: id");
        }
        if (message.getText() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Message text mustn't be empty");
        }
        if (message.getText().contains("rudeWord")) {
            throw new IllegalArgumentException("Message text does not match the rules");
        }
        return new ResponseEntity<>(this.messages.patch(message), HttpStatus.CREATED);
    }

    @PostMapping("/")
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Message> create(@Valid @RequestBody Message message) {
        if (message.getId() != 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Message id must be 0");
        }
        if (message.getText() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Message text mustn't be empty");
        }
        if (message.getText().contains("rudeWord")) {
            throw new IllegalArgumentException("Message text does not match the rules");
        }
        return new ResponseEntity<>(this.messages.save(message), HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@Valid @RequestBody Message message) {
        if (message.getId() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Missed param: id");
        }
        if (message.getText() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Message text mustn't be empty");
        }
        this.messages.save(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        try {
            messages.delete(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Message with Id: " + id + " not found");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public void exceptionHandler(Exception e, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("message", e.getMessage());
            put("type", e.getClass());
        }}));
        LOGGER.error(e.getLocalizedMessage());
    }
}
