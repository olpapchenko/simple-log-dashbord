package com.papchenko.logagent.service.impl;

import com.papchenko.logagent.service.LogSource;
import com.papchenko.logagent.service.entity.FileLogSource;
import com.papchenko.logagent.service.entity.LogSourceMetaData;
import com.papchenko.logagent.utils.FilesUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class FileLogSourceImpl implements LogSource<FileLogSource> {

    private AtomicBoolean watchLoopStarted = new AtomicBoolean(false);
    private Executor executor;
    private WatchService watchService;

    private Map<WatchDirectory, List<LogSourceMetaData>> watchedDirectoryToFile = new HashMap<>();

    @Value("${check.interval:500}")
    private long checkInterval;

    @Getter
    @Setter
    @AllArgsConstructor
    private static class WatchDirectory {
        private Path path;
        private WatchKey watchKey;

        public WatchDirectory(Path path) {
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WatchDirectory that = (WatchDirectory) o;
            return Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {

            return Objects.hash(path);
        }
    }

    public FileLogSourceImpl() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public String getLogContent(String key, int offset, int size) {
        return null;
    }

    @Override
    public synchronized void addLogSource(FileLogSource logSource) {
        Path directoryPath = logSource.getLogPath().getParent();

        WatchDirectory watchDirectory = new WatchDirectory(directoryPath);
        List<LogSourceMetaData> logSourceMetaDatas = watchedDirectoryToFile.get(watchDirectory);

        if (Objects.isNull(logSourceMetaDatas)) {
            WatchKey registerDirKey = null;
            try {
                registerDirKey = directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            watchDirectory.setWatchKey(registerDirKey);

            ArrayList<LogSourceMetaData> watchFiles = new ArrayList<>();
            watchFiles.add(new LogSourceMetaData(logSource));
            watchedDirectoryToFile.put(watchDirectory, watchFiles);
            return;
        }

        logSourceMetaDatas.add(new LogSourceMetaData(logSource));
    }

    @Override
    public synchronized void clear(String key) {
        Path file = Paths.get(key);
        clear(file.getParent());
    }

    @Override
    public synchronized void clearAll() {
        watchedDirectoryToFile
                .keySet()
                .forEach(watchDirectory -> watchDirectory.getWatchKey().cancel());

        watchedDirectoryToFile.clear();
    }

    private void clear(Path parentDir) {
        WatchDirectory watchDirectory = new WatchDirectory(parentDir);
        watchedDirectoryToFile.remove(watchDirectory);

        watchedDirectoryToFile
                .keySet()
                .stream()
                .filter(watchDirectory1 -> watchDirectory1.equals(watchDirectory))
                .findFirst()
                .ifPresent(watchDirectory1 -> watchDirectory1.getWatchKey().cancel());
    }

    @PostConstruct
    private synchronized void startWatchLoop() {
        if (watchLoopStarted.get()) {
            return;
        }

        executor.execute(() -> {
            try {
                while (true) {
                    processModifications();
                    Thread.sleep(checkInterval);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        watchLoopStarted.set(true);
    }

    private synchronized void processModifications() {
        WatchKey watchKey = watchService.poll();

        while (Objects.nonNull(watchKey)) {
            processWatchKey(watchKey);
            watchKey = watchService.poll();
        }

    }

    private void processWatchKey(WatchKey watchKey) {
        watchKey.pollEvents().stream().forEach(watchEvent -> {
            Path modifiedFile = (Path) watchEvent.context();

            Optional<LogSourceMetaData> logSource = getLogSourceByFileName(modifiedFile.getFileName());
            logSource.ifPresent(metaData -> {
                List<String> strings = FilesUtils
                        .readLines(metaData.getOffset(), metaData.getFileLogSource().getLogPath());

                metaData.setOffset(metaData.getOffset() + strings.size());

                if (CollectionUtils.isEmpty(strings)) {
                    return;
                }

                metaData.getFileLogSource().getOnModification().forEach(callback -> {
                    callback.accept(strings);
                });
            });
        });

        watchKey.reset();
    }

    private List<LogSourceMetaData> getAllLogSources() {
        return watchedDirectoryToFile
                .values()
                .stream()
                .flatMap(logSourceMetaDatas -> logSourceMetaDatas.stream())
                .collect(Collectors.toList());
    }

    private Optional<LogSourceMetaData> getLogSourceByFileName(Path fileName) {
        return getAllLogSources()
                .stream()
                .filter(metData -> metData.getFileLogSource().getLogPath().endsWith(fileName))
                .findFirst();
    }
}
