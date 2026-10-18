package com.messagingapp.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.messagingapp.service.UserService;
import com.messagingapp.dto.UserResponse;
import com.messagingapp.dto.RegisterRequest;
import com.messagingapp.entity.User;

import jakarta.validation.Valid;


@RestController 
@RequestMapping ("/users")
@RequiredArgsConstructor 
public class UserController {


    private final UserService userService;

    public UserResponse register(@Valid @RequestBody RegisterRequest request){
                User user = userService.register(request.username(), request.email(), request.password());
        return UserResponse.from(user);



    }

}
