package com.example.hrmtask.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WakeupController {

    @GetMapping("/api/wakeup")
    public ResponseEntity<String> wakeup() {
        return ResponseEntity.ok("OK");
    }
}
