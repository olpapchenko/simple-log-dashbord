package com.papchenko.logagent.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FilesUtils {

    private  FilesUtils() {
    }

    public static long getFileLinesCount(Path path) {
        try (LineNumberReader count = new LineNumberReader(new FileReader(path.toFile()))) {
            while (count.skip(Long.MAX_VALUE) > 0) {

            }
            return count.getLineNumber();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> readLines(long offset, Path path) {
        List<String> res = new ArrayList<>();
        try {
            Files.lines(path).skip(offset)
                    .forEach(s -> res.add(s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return res;
    }
}
