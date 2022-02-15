package ru.job4j.chat.service;

import org.springframework.stereotype.Service;
import ru.job4j.chat.entity.Person;
import ru.job4j.chat.repository.PersonRepository;

import java.util.Optional;

@Service
public class PersonService {
    private final PersonRepository persons;

    public PersonService(PersonRepository persons) {
        this.persons = persons;
    }

    public Iterable<Person> findAll() {
        return persons.findAll();
    }

    public Optional<Person> findById(int id) {
        return persons.findById(id);
    }

    public Person save(Person person) {
        return persons.save(person);
    }

    public void delete(Person person) {
        persons.delete(person);
    }

}
