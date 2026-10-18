package com.messagingapp.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.messagingapp.service.UserService;
import com.messagingapp.dto.UserResponse;
import com.messagingapp.dto.RegisterRequest;
import com.messagingapp.entity.User;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;



@RestController 
@RequestMapping ("/users")
@RequiredArgsConstructor 
public class UserController {


    private final UserService userService;
@PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)

    public UserResponse register(@Valid @RequestBody RegisterRequest request){
                User user = userService.register(request.username(), request.email(), request.password());
        return UserResponse.from(user);



    }

}
