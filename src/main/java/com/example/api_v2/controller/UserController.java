package com.example.api_v2.controller;



import com.example.api_v2.dto.UserDto;
import com.example.api_v2.model.User;
import com.example.api_v2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable String id) {
        UserDto user = userService.getUser(id);
        log.info("Get user by id: {}", id);
        return ResponseEntity.ok(user);
    }

    
}
