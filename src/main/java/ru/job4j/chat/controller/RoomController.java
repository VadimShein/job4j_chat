package ru.job4j.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.job4j.chat.entity.Message;
import ru.job4j.chat.entity.Room;
import ru.job4j.chat.service.RoomService;

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
    public Collection<Room> findAll() {
        HashMap<Integer, Room> rsl = new HashMap();
        List<Message> messages = rest.exchange(
                API, HttpMethod.GET, null, new ParameterizedTypeReference<List<Message>>() { }
        ).getBody();
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
        return rsl.values();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id) {
        var room = this.rooms.findById(id);
        List<Message> messages = rest.exchange(
                API, HttpMethod.GET, null, new ParameterizedTypeReference<List<Message>>() { }
        ).getBody();
        for (Message message : messages) {
            if (message.getRoomId() == room.get().getId()) {
                room.get().addMessage(message);
            }
        }
        return new ResponseEntity<>(
                room.orElse(new Room()),
                room.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        Message rsl = rest.postForObject(API, message, Message.class);
        if (rooms.findById(message.getRoomId()).isPresent()) {
            rooms.findById(message.getRoomId()).get().addMessage(message);
        }
        return new ResponseEntity<>(rsl, HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Message message) {
        rest.put(API, message);
        if (rooms.findById(message.getRoomId()).isPresent()) {
            rooms.findById(message.getRoomId()).get().addMessage(message);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        rest.delete(API_ID, id);
        return ResponseEntity.ok().build();
    }
}
