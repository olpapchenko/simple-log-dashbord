package com.papchenko.logagent.service.impl;

import com.papchenko.logagent.service.LogSource;
import com.papchenko.logagent.service.WatchRegistrationService;
import com.papchenko.logagent.service.entity.FileLogSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class WatchFileRegistrationServiceImpl implements WatchRegistrationService<String> {

    @Autowired
    private LogSource<FileLogSource> logSource;

    private Set<Path> watchedPaths = new HashSet<>();

    @Override
    public synchronized String registerNewWatchedFile(String path) {
        try {
            Path file = Paths.get(path);

            if (!Files.exists(file)) {
                log.warn("file does not exist {}", path);
                throw new RegistrationException("file does not exist " + path);
            }

            if (!watchedPaths.contains(file)) {
                registerNewFile(file);
                watchedPaths.add(file);
                log.info("new file is registered for watching");
            }

            return String.valueOf(file.hashCode());
        } catch (InvalidPathException e) {
            log.warn("invalid path provided " + path);
            throw new RegistrationException("invalid path provided " + path);
        }
    }


    private  void registerNewFile(Path path) {

        log.info("registering new file for watching");
        logSource.addLogSource(new FileLogSource(path, strings -> {

        }));
    }
}
