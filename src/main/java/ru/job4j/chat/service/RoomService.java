package ru.job4j.chat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.entity.Room;
import ru.job4j.chat.repository.RoomRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;

@Service
public class RoomService {
    private final RoomRepository rooms;

    public RoomService(RoomRepository rooms) {
        this.rooms = rooms;
    }

    public Iterable<Room> findAll() {
        return rooms.findAll();
    }

    public Optional<Room> findById(int id) {
        if (rooms.findById(id).isPresent()) {
            return rooms.findById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room is not found");
        }
    }

    public Room save(Room room) {
        return rooms.save(room);
    }

    public Room patch(Room room) throws InvocationTargetException, IllegalAccessException {
        Optional<Room> current = rooms.findById(room.getId());
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
                var newValue = getMethod.invoke(room);
                if (newValue != null) {
                    setMethod.invoke(current.get(), newValue);
                }
            }
        }
        return rooms.save(current.get());
    }

    public void delete(int id) {
        if (rooms.findById(id).isPresent()) {
            Room room = new Room();
            room.setId(id);
            rooms.delete(room);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rom id is not found");
        }

    }
}
