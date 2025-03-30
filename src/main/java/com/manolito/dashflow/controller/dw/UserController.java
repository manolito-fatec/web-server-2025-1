package com.manolito.dashflow.controller.dw;

import com.manolito.dashflow.repository.dw.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

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
