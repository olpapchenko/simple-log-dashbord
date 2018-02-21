package com.papchenko.logagent.service.impl;

import com.papchenko.logagent.service.LogSource;
import com.papchenko.logagent.service.entity.FileLogSource;
import com.papchenko.logagent.service.entity.LogSourceMetaData;
import com.papchenko.logagent.utils.FilesUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class FileLogSourceImpl implements LogSource<FileLogSource> {

    private Executor executor;
    private Map<Path, Set<LogSourceMetaData>> parentPathToLogSources = new ConcurrentHashMap<>();

    public FileLogSourceImpl() {
        executor = Executors.newSingleThreadExecutor();
    }


    @Override
    public String getLogContent(String key, int offset, int size) {
        return null;
    }

    @Override
    public void addLogSource(FileLogSource logSource) {
        startWatchLogFile(logSource);
    }

    @Override
    public void clear() {
        List<Path> paths = getAllLogSourceMetaDatas().stream()
                .map(LogSourceMetaData::getFileLogSource)
                .map(FileLogSource::getLogPath)
                .collect(Collectors.toList());

        paths.forEach(this::clear);
    }

    @Override
    public void clear(String key) {
        Path path = Paths.get(key);
        clear(path);
    }

    private void clear(Path path) {
        Set<LogSourceMetaData> logSourceMetaDatas = parentPathToLogSources.get(path.getParent());

        Optional<LogSourceMetaData> logMetaData = logSourceMetaDatas.stream()
                .filter(logSourceMetaData -> logSourceMetaData.getFileLogSource().getLogPath().equals(path))
                .findFirst();

        logMetaData.ifPresent(logSourceMetaData -> {
            logSourceMetaDatas.remove(logSourceMetaData);

            if (logSourceMetaDatas.isEmpty()) {
                logSourceMetaData.getWatchKey().cancel();
                parentPathToLogSources.remove(path.getParent());
            }
        });
    }

    private List<LogSourceMetaData> getAllLogSourceMetaDatas() {
        return parentPathToLogSources
                .entrySet()
                .stream()
                .flatMap(pathSetEntry -> pathSetEntry.getValue()
                        .stream())
                .collect(Collectors.toList());
    }

    private void startWatchLogFile(FileLogSource logSource) {
        Path parent = logSource.getLogPath().getParent();

        if (parentPathToLogSources.isEmpty()) {
            startWatchLoop();
        }

        if (parentPathToLogSources.containsKey(parent)) {
            Set<LogSourceMetaData> logSourceMetaDatas = parentPathToLogSources.get(parent);
            WatchKey watchKey = logSourceMetaDatas.iterator().next().getWatchKey();
            LogSourceMetaData logMetaData = new LogSourceMetaData(logSource);
            logMetaData.setWatchKey(watchKey);
            logSourceMetaDatas.add(logMetaData);
        } else {
            startWatchNewLogDirectory(logSource);
        }
    }

    private void startWatchNewLogDirectory(FileLogSource logSourceParams) {
        HashSet<LogSourceMetaData> logSources = new HashSet<>();
         LogSourceMetaData logMetaData = new LogSourceMetaData(logSourceParams);
        logSources.add(logMetaData);
        Path parentDir = logSourceParams.getLogPath().getParent();
        parentPathToLogSources.put(parentDir,  logSources);
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {

            final WatchKey watchKey = parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            logMetaData.setWatchKey(watchKey);
            logMetaData.setWatchService(watchService);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startWatchLoop() {
        executor.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                getAllLogSourceMetaDatas()
                        .stream()
                        .forEach(metaData -> {
                            WatchService watchService = metaData.getWatchService();
                            WatchKey wk = watchService.poll();

                            while (Objects.nonNull(wk)) {
                                processDirModificationEvent(wk, (metaData.getFileLogSource().getLogPath().getParent()));
                                wk = watchService.poll();
                            }
                        });
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
