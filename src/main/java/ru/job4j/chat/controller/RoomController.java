package ru.job4j.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.entity.Message;
import ru.job4j.chat.entity.Operation;
import ru.job4j.chat.entity.Room;
import ru.job4j.chat.service.RoomService;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomController.class.getSimpleName());
    private static final String API = "http://localhost:8080/message/";
    private static final String API_ID = "http://localhost:8080/message/{id}";
    private final RoomService rooms;

    public RoomController(RoomService rooms) {
        this.rooms = rooms;
    }

    @Autowired
    private RestTemplate rest;

    @GetMapping("/")
    public Collection<Room> findAll(@RequestHeader("Authorization") String token) {
        HashMap<Integer, Room> rsl = new HashMap<>();
        if (token != null && !token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            List<Message> messages = rest.exchange(
                    API, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Message>>() { }
            ).getBody();
            if (messages != null) {
                for (Message message : messages) {
                    if (rsl.containsKey(message.getRoomId())) {
                        rsl.get(message.getRoomId()).addMessage(message);
                    } else {
                        if (rooms.findById(message.getRoomId()).isPresent()) {
                            Room room = rooms.findById(message.getRoomId()).get();
                            room.addMessage(message);
                            rsl.put(room.getId(), room);
                        }
                    }
                }
            }
        }
        return rsl.values();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id,
                                         @RequestHeader("Authorization") String token) {
        var room = this.rooms.findById(id);
        if (room.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room is not found.");
        }
        if (token != null && !token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            List<Message> messages = rest.exchange(
                    API, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Message>>() {
                    }
            ).getBody();
            if (messages != null) {
                for (Message message : messages) {
                    if (message.getRoomId() == room.get().getId()) {
                        room.get().addMessage(message);
                    }
                }
            }
        }
        return new ResponseEntity<>(room.orElse(new Room()), HttpStatus.OK);
    }

    @PostMapping("/")
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Message> create(@Valid @RequestBody Message message,
                                          @RequestHeader("Authorization") String token) {
        if (message.getText() == null) {
            throw new NullPointerException("Message text mustn't be empty");
        }
        Message rsl = null;
        if (token != null && !token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Message> entity = new HttpEntity<>(message, headers);
            rsl = rest.postForObject(API, entity, Message.class);
        }
            return new ResponseEntity<>(rsl, HttpStatus.CREATED);
    }

    @PatchMapping("/")
    public ResponseEntity<Room> patch(@Valid @RequestBody Room room)
            throws InvocationTargetException, IllegalAccessException {
        return new ResponseEntity<>(this.rooms.patch(room), HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Message message,
                                       @RequestHeader("Authorization") String token) {
        if (message.getText() == null) {
            throw new NullPointerException("Message text mustn't be empty");
        }
        if (token != null && !token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Message> entity = new HttpEntity<>(message, headers);
            rest.put(API, entity);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @RequestHeader("Authorization") String token) {
        if (token != null && !token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            rest.delete(API_ID, id,  entity);
        }
        return ResponseEntity.ok().build();
    }
}
