package io.spring.nohttp.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;

/**
 * @author Rob Winch
 */
class IOUtils {
	static String readText(File file) {
		try {
			return new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			throw new RuntimeException("Could not read " + file, e);
		}
	}

	static void writeTextTo(String text, File file) {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
			writer.write(text);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
