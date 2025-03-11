package com.example.api_v2.controller;



import com.example.api_v2.dto.UserDto;
import com.example.api_v2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable("id") String id) {
        UserDto user = userService.getUser(id);
        log.info("Get user by id: {}", id);
        return ResponseEntity.ok(user);
    }

    
}
