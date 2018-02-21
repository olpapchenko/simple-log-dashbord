package com.papchenko.logagent.service.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@EqualsAndHashCode
public class FileLogSource {
    private Path logPath;
    private List<Consumer<List<String>>> onModification = new ArrayList<>();

    public FileLogSource(Path logPath, Consumer<List<String>> onModification) {
        this.logPath = logPath;
        this.onModification.add(onModification);
    }
}
