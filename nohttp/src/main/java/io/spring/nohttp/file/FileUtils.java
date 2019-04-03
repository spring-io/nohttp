/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.nohttp.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;

/**
 * Utilities for working with {@link File}s
 * @author Rob Winch
 */
abstract class FileUtils {

	/**
	 * Reads text from a file
	 * @param file the file to read from
	 * @return the text within the {@link File}
	 */
	static String readTextFrom(File file) {
		assertValidFile(file);
		try {
			return new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read " + file, e);
		}
	}

	/**
	 * Writes text to a file overriding any existing text
	 * @param text the text to write to the {@link File}
	 * @param file the {@link File} to write to
	 */
	static void writeTextTo(String text, File file) {
		if (text == null) {
			throw new IllegalArgumentException("text cannot be null");
		}
		assertValidFile(file);
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
			writer.write(text);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write to " + file, e);
		}
	}

	private static void assertValidFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("file cannot be null");
		}
	}

	private FileUtils() {}
}
