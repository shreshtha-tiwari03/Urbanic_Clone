package com.example.democart.Model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @jakarta.persistence.Id
    private Long Id;
    private String name;
    private String Email;
    private String Password;
    private int Mobile;
    private int Address;

}
