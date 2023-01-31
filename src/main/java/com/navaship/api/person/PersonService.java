package com.navaship.api.person;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PersonService {
    private PersonRepository personRepository;


    public Person createPerson(Person person) {
        return personRepository.save(person);
    }
}
