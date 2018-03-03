package com.papchenko.logagent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;

public final class TestUtils {
	private static final String testFilePath1 = "testLogFile1.txt";
	private static final String testFilePath2 = "testLogFile2.txt";

	public static Path getTextFilePath1() {
		ClassLoader classLoader = TestUtils.class.getClassLoader();
		File file = new File(classLoader.getResource(testFilePath1).getFile());

		return file.toPath();
	}

	public static Path getTextFilePath2() {
 		ClassLoader classLoader = TestUtils.class.getClassLoader();
		File file = new File(classLoader.getResource(testFilePath2).getFile());

		return file.toPath();
	}

	public static void clearFile(Path file) {
		try(PrintWriter writer = new PrintWriter(file.toFile())) {
			writer.write("");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
