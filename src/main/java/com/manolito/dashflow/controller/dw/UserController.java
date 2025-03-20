package com.manolito.dashflow.controller.dw;

import com.manolito.dashflow.repository.dw.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user")
    @ResponseBody
    public ResponseEntity<Long> getUserIdByOriginalId(@RequestParam String originalId) {
        Long userId = userRepository.getUserIdByOriginalId(originalId);
        if (userId != null) {
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
