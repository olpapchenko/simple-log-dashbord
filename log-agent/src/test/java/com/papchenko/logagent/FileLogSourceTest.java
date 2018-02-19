package com.papchenko.logagent;

import com.papchenko.logagent.service.LogSource;
import com.papchenko.logagent.service.impl.FileLogSourceImpl;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileLogSourceTest {

    private static final String testFilePath = "testLogFIle.txt";

    @Autowired
    private LogSource<FileLogSourceImpl.FileLogSource> fileLogSource;

    @After
    public void clearFile() throws IOException {
        PrintWriter writer = new PrintWriter(getTextFilePath().toFile());
        writer.print("");
        writer.close();
    }

    @Test
    public void testLogFileModificationCallback() throws IOException, InterruptedException {

        List<String> expectedStrings = Arrays.asList("1", "2");
        List<String>[] actualStrings = new List [1];

        fileLogSource.addLogSource(new FileLogSourceImpl.FileLogSource(getTextFilePath(), strings -> {
            actualStrings[0] = strings;
        }));

        Files.write(getTextFilePath(), expectedStrings,  StandardOpenOption.APPEND);

        Thread.sleep(1500);

        assertNotNull(actualStrings[0]);
        assertEquals(expectedStrings, actualStrings[0]);
    }

    private Path getTextFilePath() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(testFilePath).getFile());

        return file.toPath();
    }
}
