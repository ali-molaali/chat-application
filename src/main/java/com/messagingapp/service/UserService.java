package com.messagingapp.service;

import com.messagingapp.entity.User;

public interface UserService {

    User register(String username, String email, String rawPassword);

}
