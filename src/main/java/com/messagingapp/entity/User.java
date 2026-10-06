package com.messagingapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity 
@Table 
@Setter 
@Getter 
public class User {
    private Long id;

    private String username;

}
