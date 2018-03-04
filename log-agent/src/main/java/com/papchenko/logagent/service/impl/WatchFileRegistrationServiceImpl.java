package com.papchenko.logagent.service.impl;

import com.google.common.hash.Hashing;
import com.papchenko.logagent.dto.LogSourceUpdateDto;
import com.papchenko.logagent.service.LogSource;
import com.papchenko.logagent.service.WatchRegistrationService;
import com.papchenko.logagent.service.entity.FileLogSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Controller
public class WatchFileRegistrationServiceImpl implements WatchRegistrationService<Path> {

    private Set<Path> watchedPaths = new HashSet<>();
    private Set<Path> changedFiles = new HashSet<>();

    private static final String TOPIC_PATTERN = "/topic/change/%s";

    private static final long CLEAN_LOOP_DELAY = 10000L;

    @Autowired
    private LogSource<FileLogSource> logSource;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public synchronized String registerWatchedFile(Path file) {
        if (!Files.exists(file)) {
            log.warn("file does not exist {}", file.toString());
            throw new RegistrationException("file does not exist " + file.toString());
        }

        if (!watchedPaths.contains(file)) {
            registerNewFile(file);
            watchedPaths.add(file);
            log.info("new file is registered for watching {}", getWatchedFileId(file));
        } else {
            log.info("file is already watched by service returning existing file key {}", getWatchedFileId(file));
        }

        return getWatchedFileId(file);
    }

    @Override
    public synchronized void notifyMessageConsumed(String key) {
        log.info("notified messages consumed for key {}", key);
        changedFiles.removeIf(path -> getWatchedFileId(path).equals(key));
        log.info("changed files still for cleaning {}", changedFiles.size());
    }

    private void notifyFileChanged(List<String> data, String id) {
        log.info("notify for file changed: {}", id);
        simpMessagingTemplate.convertAndSend(String.format(TOPIC_PATTERN, id), new LogSourceUpdateDto(id, data));
    }

    @Scheduled(fixedDelay = CLEAN_LOOP_DELAY)
    private void startCleaningLoop() {
            log.info("start cleaning of unused log watches");
            clearUnusedWatchPaths();
            log.info("end cleaning of unused log watches");
    }

    private synchronized void clearUnusedWatchPaths() {
        log.info("clearing unused watch files count {}", changedFiles.size());
        changedFiles.forEach(path -> logSource.clear(path.toString()));
        watchedPaths.removeAll(changedFiles);
        changedFiles.clear();
    }


    private void registerNewFile(Path path) {
        log.info("registering new file for watching");
        logSource.addLogSource(new FileLogSource(path, strings -> handleFileChange(path, strings)));
    }

    private synchronized void handleFileChange(Path changedPath, List<String> strings) {
        changedFiles.add(changedPath);
        notifyFileChanged(strings, getWatchedFileId(changedPath));
    }

    private static String getWatchedFileId(Path path) {
        return Hashing
                .sha256()
                .hashString(path.toString().toLowerCase(), StandardCharsets.UTF_8)
                .toString();
    }
}
