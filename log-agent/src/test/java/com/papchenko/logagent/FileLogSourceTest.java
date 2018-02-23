package com.papchenko.logagent;

import com.papchenko.logagent.service.LogSource;
import com.papchenko.logagent.service.entity.FileLogSource;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FileLogSourceTest {

    private static final String testFilePath1 = "testLogFIle1.txt";
    private static final String testFilePath2 = "testLogFIle2.txt";

    @Autowired
    private LogSource<FileLogSource> fileLogSource;

    @After
    public void clearFile() {
        clearFile(getTextFilePath1());
        clearFile(getTextFilePath2());

        fileLogSource.clearAll();
    }

    @Test
    public void testLogFileModificationCallback() throws IOException, InterruptedException {

        List<String> expectedStrings = Arrays.asList("1", "2");
        List<String>[] actualStrings = new List [1];

        fileLogSource.addLogSource(new FileLogSource(getTextFilePath1(), strings -> {
            actualStrings[0] = strings;
        }));

        Files.write(getTextFilePath1(), expectedStrings,  StandardOpenOption.APPEND);

        Thread.sleep(1500);

        assertNotNull(actualStrings[0]);
        assertEquals(expectedStrings, actualStrings[0]);
    }

    @Test
    public void testNonEmptyFile() throws IOException, InterruptedException {
        List<String> expectedStrings = Arrays.asList("1", "2", "5", "6");
        List<String> actualStrings = new ArrayList<>();
        Files.write(getTextFilePath1(), expectedStrings,  StandardOpenOption.APPEND);

        fileLogSource.addLogSource(new FileLogSource(getTextFilePath1(), strings -> {
            actualStrings.addAll(strings);
        }));

        Thread.sleep(100);

        Files.write(getTextFilePath1(),  Arrays.asList(expectedStrings.get(0), expectedStrings.get(1)),
                StandardOpenOption.APPEND);

        Files.write(getTextFilePath1(), Arrays.asList(expectedStrings.get(2), expectedStrings.get(3)),
                StandardOpenOption.APPEND);

        Thread.sleep(1200);
        assertNotEquals(0, actualStrings.size());
        assertEquals(expectedStrings, actualStrings);
    }

    @Test
    public void testRemoveEntry() throws IOException {
        boolean [] invokedCallbacks = new boolean[2];

        fileLogSource.addLogSource(new FileLogSource(getTextFilePath1(), strings -> {
            invokedCallbacks[0] = true;
        }));


        fileLogSource.addLogSource(new FileLogSource(getTextFilePath2(), strings -> {
            invokedCallbacks[1] = true;
        }));

        fileLogSource.clear(getTextFilePath1().toString());
        fileLogSource.clear(getTextFilePath2().toString());

        Files.write(getTextFilePath1(), Arrays.asList("1"),  StandardOpenOption.APPEND);
        Files.write(getTextFilePath2(), Arrays.asList("1"),  StandardOpenOption.APPEND);

        assertFalse(invokedCallbacks[0]);
        assertFalse(invokedCallbacks[1]);
    }

    private void clearFile(Path file) {
        try(PrintWriter writer = new PrintWriter(file.toFile())) {
            writer.write("");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getTextFilePath1() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(testFilePath1).getFile());

        return file.toPath();
    }

    private Path getTextFilePath2() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(testFilePath2).getFile());

        return file.toPath();
    }
}
