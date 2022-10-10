package ru.job4j.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.EmptyResultDataAccessException;
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
        HashMap<Integer, Room> rooms = new HashMap<>();
        if (token != null && !token.isEmpty()) {
            this.rooms.findAll().forEach(e -> rooms.put(e.getId(), e));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            List<Message> messages = rest.exchange(
                    API, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Message>>() { }
            ).getBody();
            if (messages != null) {
                for (Message message : messages) {
                    rooms.get(message.getRoomId()).addMessage(message);
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Authorization token is not found");
        }
        return rooms.values();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id, @RequestHeader("Authorization") String token) {
        Room room;
        if (this.rooms.findById(id).isPresent()) {
            room = this.rooms.findById(id).get();
            if (token != null && !token.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", token);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                List<Message> messages = rest.exchange(
                        API + "roomId/" + id, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Message>>() { }
                ).getBody();
                if (messages != null) {
                    for (Message message : messages) {
                        room.addMessage(message);
                    }
                }
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Authorization token is not found");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(room, HttpStatus.OK);
    }

    @PostMapping("/")
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Room> create(@Valid @RequestBody Room room) {
        if (room.getId() != 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Room id must be 0");
        }
        if (room.getName() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Name of room mustn't be empty");
        }
        if (room.getDescription() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Description of room mustn't be empty");
        }
        return new ResponseEntity<>(this.rooms.save(room), HttpStatus.CREATED);
    }

    @PatchMapping("/")
    public ResponseEntity<Room> patch(@Valid @RequestBody Room room) throws InvocationTargetException, IllegalAccessException {
        if (room.getId() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Missed param: id");
        }
        if (room.getName() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Name of room mustn't be empty");
        }
        if (room.getDescription() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Description of room mustn't be empty");
        }
        return new ResponseEntity<>(this.rooms.patch(room), HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@Valid @RequestBody Room room) {
        if (room.getId() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Missed param: id");
        }
        if (room.getName() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Name of room mustn't be empty");
        }
        if (room.getDescription() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Description of room mustn't be empty");
        }
        this.rooms.save(room);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        try {
            rooms.delete(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "room with Id: " + id + " is not found");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/message")
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Message> create(@Valid @RequestBody Message message,
                                          @RequestHeader("Authorization") String token) {
        if (message.getId() != 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Message id must be 0");
        }
        if (message.getText() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Message text mustn't be empty");
        }
        Message rsl;
        if (token != null && !token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Message> entity = new HttpEntity<>(message, headers);
            rsl = rest.postForObject(API, entity, Message.class);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Authorization token is not found");
        }
            return new ResponseEntity<>(rsl, HttpStatus.CREATED);
    }

    @PutMapping("/message")
    public ResponseEntity<Void> update(@RequestBody Message message,
                                       @RequestHeader("Authorization") String token) {
        if (message.getId() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Missed param: id");
        }
        if (message.getText() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Message text mustn't be empty");
        }
        if (token != null && !token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Message> entity = new HttpEntity<>(message, headers);
            rest.put(API, entity);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Authorization token is not found");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id,
                                       @RequestHeader("Authorization") String token) {
        if (token != null && !token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            rest.delete(API_ID, id, entity);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Authorization token is not found");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
