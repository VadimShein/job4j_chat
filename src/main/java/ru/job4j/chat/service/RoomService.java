package ru.job4j.chat.service;

import org.springframework.stereotype.Service;
import ru.job4j.chat.entity.Room;
import ru.job4j.chat.repository.RoomRepository;

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
        return rooms.findById(id);
    }

    public Room save(Room room) {
        return rooms.save(room);
    }

    public void delete(Room room) {
        rooms.delete(room);
    }
}
