package com.example.backend.Service;

import com.example.backend.Entity.User;

import java.util.List;

public interface IUserService {
    User registerUser(User request);
    public boolean isValidCredentials(User credentials) ;
    public List<User> getAllUsers();
    public void updateStatus(Long id, boolean newValue);
    public void registerUserAdmin(User request);

}
