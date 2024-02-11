package com.example.backend.Controller;

import com.example.backend.Entity.User;
import com.example.backend.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/User")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User request) {
        userService.registerUser(request);
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "User registered successfully");
        return ResponseEntity.ok(responseBody);
    }


}
