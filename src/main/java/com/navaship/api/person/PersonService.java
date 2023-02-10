package com.navaship.api.person;

import com.navaship.api.shipment.Shipment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PersonService {
    private PersonRepository personRepository;


    public Person createPerson(Shipment shipment, String name, String company, String phoneNumber, String email, PersonType type) {
        Person person = new Person();
        person.setShipment(shipment);
        person.setName(name);
        person.setCompany(company);
        person.setPhoneNumber(phoneNumber);
        person.setEmail(email);
        person.setType(type);
        return personRepository.save(person);
    }
}
