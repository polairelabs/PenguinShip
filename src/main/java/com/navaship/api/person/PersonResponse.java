package com.navaship.api.person;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonResponse {
    private Long id;
    private String name;
    private String company;
    private String phoneNumber;
    private String email;
    @Enumerated(EnumType.STRING)
    private PersonType type;
}
