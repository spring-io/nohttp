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
import java.util.function.Predicate;

import static io.spring.nohttp.file.FilePredicates.fileHasName;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class DirScannerTest {

	private static final File SOURCES_DIR = new File("src/test/resources/file/dirscanner");

	private List<String> fileNames = new ArrayList<>();

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	// create

	@Test
	public void createWhenNullThenIllegalArgumentException() {
		assertThatCode(() -> DirScanner.create(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("dir cannot be null");
	}

	@Test
	public void createWhenNotDirThenIllegalArgumentException() throws IOException {
		File file = this.temp.newFile();
		assertThatCode(() -> DirScanner.create(file))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(file + " is not a directory");
	}

	// textFiles

	@Test
	public void scanWhenTextFilesTrueThenSkipsBinaryFiles() {
		File dir = new File(SOURCES_DIR, "textfiles");

		DirScanner.create(dir)
				.textFiles(true)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("Test.java");
	}

	@Test
	public void scanWhenTextFilesFalseThenDoesNotSkipBinaryFiles() {
		File dir = new File(SOURCES_DIR, "textfiles");

		DirScanner.create(dir)
				.textFiles(false)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("Test.java", "Test.class", "textfiles.jar");
	}

	@Test
	public void scanWhenTextFilesDefaultThenDoesNotSkipBinaryFiles() {
		File dir = new File(SOURCES_DIR, "textfiles");

		DirScanner.create(dir)
				.textFiles(false)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("Test.java", "Test.class", "textfiles.jar");
	}

	// excludeDirs

	@Test
	public void excludeDirsWhenNullThenIllegalArgumentException() throws IOException {
		DirScanner scanner = DirScanner.create(this.temp.newFolder());
		assertThatCode(() -> scanner.excludeDirs(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("dirExclusion cannot be null");
	}

	@Test
	public void scanWhenNotDirThenIllegalArgumentException() throws IOException {
		File file = this.temp.newFile();
		assertThatCode(() -> DirScanner.create(file))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(file + " is not a directory");
	}

	@Test
	public void scanWhenExcludeDirsThenSkipsSubtree() {
		File dir = new File(SOURCES_DIR, "excludedirs/simple");

		DirScanner.create(dir)
				.excludeDirs(fileHasName("git"))
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("found.txt");
	}

	@Test
	public void scanWhenExcludeDirsDefaultThenDoesNotSkipSubtree() {
		File dir = new File(SOURCES_DIR, "excludedirs/simple");

		DirScanner.create(dir)
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("found.txt", "a.txt", "b.txt");
	}

	@Test
	public void scanWhenExcludeDirsMultiThenSkipsBoth() {
		File dir = new File(SOURCES_DIR, "excludedirs/multi");

		DirScanner.create(dir)
				.excludeDirs(fileHasName("a"))
				.excludeDirs(fileHasName("b"))
				.scan(this::collectFileNames);

		assertThat(this.fileNames).isEmpty();
	}

	// excludeFiles

	@Test
	public void excludeFilesWhenNullThenIllegalArgumentException() throws IOException {
		DirScanner scanner = DirScanner.create(this.temp.newFolder());
		assertThatCode(() -> scanner.excludeFiles(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("fileExclusion cannot be null");
	}

	@Test
	public void scanWhenExcludeFilesThenSkips() {
		File dir = new File(SOURCES_DIR, "excludefiles");

		DirScanner.create(dir)
				.excludeFiles(fileHasName("a.txt"))
				.scan(this::collectFileNames);

		assertThat(this.fileNames).containsOnly("b.txt");
	}

	@Test
	public void scanWhenExcludeFilesMultiThenSkipsBoth() {
		File dir = new File(SOURCES_DIR, "excludefiles");

		DirScanner.create(dir)
				.excludeFiles(fileHasName("a.txt"))
				.excludeFiles(fileHasName("b.txt"))
				.scan(this::collectFileNames);

		assertThat(this.fileNames).isEmpty();
	}

	// scan

	@Test
	public void scanWhenFileProcessorNullThenIllegalArgumentException() throws IOException {
		DirScanner scanner = DirScanner.create(this.temp.newFolder());
		assertThatCode(() -> scanner.scan(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("fileProcessor cannot be null");
	}

	private void collectFileNames(File file) {
		this.fileNames.add(file.getName());
	}
}