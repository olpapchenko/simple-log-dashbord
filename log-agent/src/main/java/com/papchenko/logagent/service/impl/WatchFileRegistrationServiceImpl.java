package com.papchenko.logagent.service.impl;

import com.google.common.hash.Hashing;
import com.papchenko.logagent.dto.LogSourceUpdateDto;
import com.papchenko.logagent.service.LogSource;
import com.papchenko.logagent.service.WatchRegistrationService;
import com.papchenko.logagent.service.entity.FileLogSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Controller
public class WatchFileRegistrationServiceImpl implements WatchRegistrationService<Path> {

    private ExecutorService executor;
    private Set<Path> watchedPaths = new HashSet<>();
    private Set<Path> changedFiles = new HashSet<>();
    private AtomicBoolean cleanLoopStarted = new AtomicBoolean();

    private static final String TOPIC_PATTERN = "/topic/change/%s";

    @Value("${clear.timeout:10000}")
    private Long clearTimeout;

    @Autowired
    private LogSource<FileLogSource> logSource;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public WatchFileRegistrationServiceImpl() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public synchronized String registerNewWatchedFile(Path file) {
        if (!Files.exists(file)) {
            log.warn("file does not exist {}", file.toString());
            throw new RegistrationException("file does not exist " + file.toString());
        }

        if (!watchedPaths.contains(file)) {
            registerNewFile(file);
            watchedPaths.add(file);
            log.info("new file is registered for watching");
        }

        return getWatchedFileId(file);
    }

    @Override
    public synchronized void notifyMessageConsumed(String logId) {
        changedFiles.removeIf(path -> getWatchedFileId(path).equals(logId));
    }

    private void notifyFileChanged(List<String> data, String id) {
        simpMessagingTemplate.convertAndSend(String.format(TOPIC_PATTERN, id), new LogSourceUpdateDto(id, data));
    }

    @PostConstruct
    private void startCleaningLoop() {
        synchronized (cleanLoopStarted) {
            if (cleanLoopStarted.get()) {
                log.info("clean loop already started - doing nothing");
                return ;
            }

            log.info("start cleaning loop");

            executor.execute(() -> {

                while (true) {
                    log.info("start cleaning");

                    clearUnusedWatchPaths();

                    try {
                        Thread.sleep(clearTimeout);
                    } catch (InterruptedException e) {
                        log.error("unexpected error occurred", e);
                        throw new RuntimeException(e);
                    }
                }
            });
            cleanLoopStarted.set(true);
        }
    }

    private synchronized void clearUnusedWatchPaths() {
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
