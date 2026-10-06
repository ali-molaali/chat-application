package com.messagingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ChatApplicationMain {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplicationMain.class, args);
    }
}
