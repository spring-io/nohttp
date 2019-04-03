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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class PreGradle21ScannerTest {
	private static final File SOURCES_DIR = new File("src/test/resources/file/gradlehttpdslscanner");

	private List<String> fileNames = new ArrayList<>();

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	// create

	@Test
	public void createWhenNullThenIllegalArgumentException() {
		assertThatCode(() -> PreGradle21Scanner.create(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("dir cannot be null");
	}

	@Test
	public void createWhenNotDirThenIllegalArgumentException() throws IOException {
		File file = this.temp.newFile();
		assertThatCode(() -> PreGradle21Scanner.create(file))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(file + " is not a directory");
	}

	// scan

	@Test
	public void scanWhenNullThenIllegalArgumentException() throws IOException {
		File file = this.temp.newFolder();
		PreGradle21Scanner scanner = PreGradle21Scanner.create(file);
		assertThatCode(() -> scanner.scan(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("fileProcessor cannot be null");
	}

	@Test
	public void scanWhenGradle0ThenFindsAllDotGradle() throws IOException {
		File dir = new File(SOURCES_DIR, "gradle0");

		PreGradle21Scanner.create(dir)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("build.gradle", "jcenter.gradle");
	}

	@Test
	public void scanWhenGradle1ThenFindsAllDotGradle() throws IOException {
		File dir = new File(SOURCES_DIR, "gradle1");

		PreGradle21Scanner.create(dir)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("build.gradle", "jcenter.gradle");
	}

	@Test
	public void scanWhenGradle2Dot0ThenFindsAllDotGradle() throws IOException {
		File dir = new File(SOURCES_DIR, "gradle2.0");

		PreGradle21Scanner.create(dir)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("build.gradle", "jcenter.gradle");
	}

	@Test
	public void scanWhenGradle2Dot1ThenFindsNone() throws IOException {
		File dir = new File(SOURCES_DIR, "gradle2.1");

		PreGradle21Scanner.create(dir)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).isEmpty();
	}

	@Test
	public void scanWhenNoWrapperThenFindsNone() throws IOException {
		File dir = new File(SOURCES_DIR, "nogradlewrapper");

		PreGradle21Scanner.create(dir)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).isEmpty();
	}

	@Test
	public void scanWhenNoDotGradleThenFindsNone() throws IOException {
		File dir = new File(SOURCES_DIR, "nodotgradlefiles");

		PreGradle21Scanner.create(dir)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).isEmpty();
	}

	private void collectFileNames(File file) {
		this.fileNames.add(file.getName());
	}
}