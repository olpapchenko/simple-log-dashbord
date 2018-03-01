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
import java.util.stream.IntStream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FileLogSourceTest {

    TestUtils testUtils = new TestUtils();

    @Autowired
    private LogSource<FileLogSource> fileLogSource;

    @After
    public void clearFile() {
        testUtils.clearFile(testUtils.getTextFilePath1());
        testUtils.clearFile(testUtils.getTextFilePath2());

        fileLogSource.clearAll();
    }

    @Test
    public void testLogFileModificationCallback() throws IOException, InterruptedException {

        List<String> expectedStrings = Arrays.asList("1", "2");
        List<String>[] actualStrings = new List [1];

        fileLogSource.addLogSource(new FileLogSource(TestUtils.getTextFilePath1(), strings -> {
            actualStrings[0] = strings;
        }));

        Files.write(TestUtils.getTextFilePath1(), expectedStrings,  StandardOpenOption.APPEND);

        Thread.sleep(1500);

        assertNotNull(actualStrings[0]);
        assertEquals(expectedStrings, actualStrings[0]);
    }

    @Test
    public void testNonEmptyFile() throws IOException, InterruptedException {
        List<String> expectedStrings = Arrays.asList("1", "2", "5", "6");
        List<String> actualStrings = new ArrayList<>();
        Files.write(TestUtils.getTextFilePath1(), expectedStrings,  StandardOpenOption.APPEND);

        fileLogSource.addLogSource(new FileLogSource(TestUtils.getTextFilePath1(), strings -> {
            actualStrings.addAll(strings);
        }));

        Thread.sleep(100);

        Files.write(TestUtils.getTextFilePath1(),  Arrays.asList(expectedStrings.get(0), expectedStrings.get(1)),
                StandardOpenOption.APPEND);

        Files.write(TestUtils.getTextFilePath1(), Arrays.asList(expectedStrings.get(2), expectedStrings.get(3)),
                StandardOpenOption.APPEND);

        Thread.sleep(1200);
        assertNotEquals(0, actualStrings.size());
        assertEquals(expectedStrings, actualStrings);
    }

    @Test
    public void testRemoveEntry() throws IOException {
        boolean [] invokedCallbacks = new boolean[2];

        fileLogSource.addLogSource(new FileLogSource(TestUtils.getTextFilePath1(), strings -> {
            invokedCallbacks[0] = true;
        }));


        fileLogSource.addLogSource(new FileLogSource(testUtils.getTextFilePath2(), strings -> {
            invokedCallbacks[1] = true;
        }));

        fileLogSource.clear(testUtils.getTextFilePath1().toString());
        fileLogSource.clear(testUtils.getTextFilePath2().toString());

        Files.write(testUtils.getTextFilePath1(), Arrays.asList("1"),  StandardOpenOption.APPEND);
        Files.write(testUtils.getTextFilePath2(), Arrays.asList("1"),  StandardOpenOption.APPEND);

        assertFalse(invokedCallbacks[0]);
        assertFalse(invokedCallbacks[1]);
    }

    @Test
    public void intensiveWritesTest() throws InterruptedException {
        List<String> actual = new ArrayList<>();
        List<String> expected = new ArrayList<>();

        fileLogSource.addLogSource(new FileLogSource(TestUtils.getTextFilePath1(), strings -> {
            actual.addAll(strings);
        }));

        IntStream.range(1, 300).forEach(value -> {
            try {
                String s = String.valueOf(value);
                expected.add(s);
                Files.write(TestUtils.getTextFilePath1(), Arrays.asList(s), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        Thread.sleep(1000);
        assertEquals(expected, actual);
    }



 
}
