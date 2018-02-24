package com.papchenko.logagent.web;

import com.papchenko.logagent.service.WatchRegistrationService;
import com.papchenko.logagent.service.impl.RegistrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@Slf4j
@RestController
@RequestMapping("/watch")
public class WatchController {

    @Autowired
    private WatchRegistrationService<Path> watchRegistrationService;

    @PutMapping("/{path}")
    public ResponseEntity<String>  registerWatchedFile(@PathParam("path") String path) {
        try {
            Path file = Paths.get(path);
            return ResponseEntity.ok(watchRegistrationService.registerNewWatchedFile(file));
        } catch (InvalidPathException e) {
            log.warn("path does not exist {}", path);
            return ResponseEntity.badRequest().body("Path does not exists");
        } catch (RegistrationException e) {
            log.warn("failed to register path " + path, e);
            return ResponseEntity.badRequest().body("failed to register file for watching");
        }
    }

    @PutMapping("/{id}")
    public void notifyLogChangeConsumed(@PathParam("id") String id) {
        watchRegistrationService.notifyMessageConsumed(id);
        log.debug("change consumed {}", id);
    }
}
