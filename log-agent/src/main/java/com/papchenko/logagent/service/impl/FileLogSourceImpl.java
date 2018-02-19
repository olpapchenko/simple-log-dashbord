package com.papchenko.logagent.service.impl;

import com.papchenko.logagent.service.LogSource;
import com.papchenko.logagent.utils.FilesUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
public class FileLogSourceImpl implements LogSource<FileLogSourceImpl.FileLogSource> {

    private Executor executor;
    private Map<Path, Set<LogSourceMetaData>> parentPathToLogSources = new HashMap<>();

    public FileLogSourceImpl() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Getter
    @EqualsAndHashCode
    public static class FileLogSource {
        private Path logPath;
        private List<Consumer<List<String>>> onModification = new ArrayList<>();

        public FileLogSource(Path logPath, Consumer<List<String>> onModification) {
            this.logPath = logPath;
            this.onModification.add(onModification);
        }
    }

    @Data
    private static class LogSourceMetaData {
        private long offset;
        private FileLogSource FileLogSource;

        public LogSourceMetaData(FileLogSource fileLogSource) {
            FileLogSource = fileLogSource;
            offset = FilesUtils.getFileLinesCount(fileLogSource.getLogPath());
        }
    }

    @Override
    public String getLogContent(String key, int offset, int size) {
        return null;
    }

    @Override
    public void addLogSource(FileLogSource logSource) {
        startWatchLogFile(logSource);
    }

    private void startWatchLogFile(FileLogSource logSource) {
        if (parentPathToLogSources.containsKey(logSource.getLogPath().getParent())) {
            parentPathToLogSources.get(logSource.getLogPath()).add(new LogSourceMetaData(logSource));
        } else {
            startWatchNewLogDirectory(logSource);
        }
    }

    private void startWatchNewLogDirectory(FileLogSource logSourceParams) {
        HashSet<LogSourceMetaData> logSources = new HashSet<>();
        logSources.add(new LogSourceMetaData(logSourceParams));
        Path parentDir = logSourceParams.getLogPath().getParent();
        parentPathToLogSources.put(parentDir,  logSources);

        executor.execute(() -> {
            try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                final WatchKey watchKey = parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    Thread.sleep(1000);
                    WatchKey wk = watchService.poll();

                    while (Objects.nonNull(wk)) {
                        processDirModificationEvent(watchKey, parentDir);
                        wk = watchService.poll();
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void processDirModificationEvent(WatchKey watchKey, Path parentDirPath) {
        watchKey.pollEvents().stream().forEach(watchEvent -> {
            Path pathOfChangedFile = (Path) watchEvent.context();

            Optional<LogSourceMetaData> logSourceMetaData = getByParentAndFilePath(parentDirPath, pathOfChangedFile);

            logSourceMetaData.ifPresent(logMeta -> {
                List<String> logStrings = FilesUtils
                        .readLines(logMeta.getOffset(), logMeta.getFileLogSource().getLogPath());
                logMeta.setOffset(logMeta.getOffset() + logStrings.size());

                logMeta.getFileLogSource().getOnModification()
                        .forEach(onModification -> onModification.accept(logStrings));
            });
        });
    }


    private Optional<LogSourceMetaData> getByParentAndFilePath(Path parentDir, Path filePath) {
        Set<LogSourceMetaData> logSourceMeta = parentPathToLogSources.get(parentDir);
        if (Objects.isNull(logSourceMeta)) {
            return Optional.empty();
        }

        return parentPathToLogSources
                .get(parentDir)
                .stream()
                .filter(logSourceMetaData ->
                logSourceMetaData.getFileLogSource().getLogPath().endsWith(filePath.getFileName()))
                .findFirst();
    }
}
