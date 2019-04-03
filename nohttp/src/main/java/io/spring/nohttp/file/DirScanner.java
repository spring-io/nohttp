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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Used for scanning a directory and processing files.
 *
 * Example usage:
 *
 * <pre>
 * DirScanner.create(this.dir)
 *     // only process text based files
 *     .textFiles(true)
 *     // do not process directories named .git or their contents
 *     .excludeDirs(FilePredicates.hasFileName(".git"))
 *     // only process files that end with ".java"
 *     .excludeFiles(f -> !f.getName().endsWith(".java"))
 *     // for each match print out the file name
 *     .scan(System.out::println);
 * </pre>
 *
 * @author Rob Winch
 */
public class DirScanner {
	private final File dir;

	private List<Predicate<File>> excludeDirs = new ArrayList<>();

	private List<Predicate<File>> excludeFiles = new ArrayList<>();

	private DirScanner(File dir) {
		if (dir == null) {
			throw new IllegalArgumentException("dir cannot be null");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory");
		}
		this.dir = dir;
	}

	/**
	 * Creates a new instance
	 * @param dir the directory to scan
	 * @return the {@link DirScanner} to use
	 */
	public static DirScanner create(File dir) {
		return new DirScanner(dir);
	}

	/**
	 * Indicates that only text based files should be used. The implementation uses
	 * a native invocation of grep to determine if the file is a text based file. If
	 * this does not work, you will need to use {@link #excludeFiles(Predicate)} to
	 * indicate what files should be processed.
	 *
	 * @param textFilesOnly true if only text based files should be process, else false
	 * @return the {@link DirScanner} to use
	 */
	public DirScanner textFiles(boolean textFilesOnly) {
		if (textFilesOnly) {
			return excludeFiles(f -> !isTextFile(f));
		} else {
			return this;
		}
	}

	private static boolean isTextFile(File file) {
		try {
			return new ProcessBuilder("grep", "-Iq", ".", file.getAbsolutePath())
					.directory(file.getParentFile())
					.start()
					.waitFor() == 0;
		} catch (Exception e) {
			throw new RuntimeException("Could not determine if " + file + " is a text file. Try explicitly providing the dir or files you want to process", e);
		}
	}

	/**
	 * Exclude additional directories that match the provided predicate
	 * @param dirExclusion the Predicate that returns true for directories that should be excluded
	 * @return the FileScanner for additional customizations
	 */
	public DirScanner excludeDirs(Predicate<File> dirExclusion) {
		if (dirExclusion == null) {
			throw new IllegalArgumentException("dirExclusion cannot be null");
		}
		this.excludeDirs.add(dirExclusion);
		return this;
	}

	/**
	 * Exclude additional files that match the provided predicate
	 * @param fileExclusion the Predicate that returns true for files that should be excluded
	 * @return the FileScanner for additional customizations
	 */
	public DirScanner excludeFiles(Predicate<File> fileExclusion) {
		if (fileExclusion == null) {
			throw new IllegalArgumentException("fileExclusion cannot be null");
		}
		this.excludeFiles.add(fileExclusion);
		return this;
	}

	/**
	 * Scans the directory provided in {@link #create(File)} and passes any matching
	 * {@link File} to the provided {@link Consumer}
	 * @param fileProcessor the {@link Consumer} to process each matching file
	 */
	public void scan(Consumer<File> fileProcessor) {
		if (fileProcessor == null) {
			throw new IllegalArgumentException("fileProcessor cannot be null");
		}
		FileScannerVisitor visitor = new FileScannerVisitor(fileProcessor);
		try {
			Files.walkFileTree(this.dir.toPath(), visitor);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	class FileScannerVisitor extends SimpleFileVisitor<Path> {
		private final Consumer<File> fileProcessor;

		FileScannerVisitor(Consumer<File> fileProcessor) {
			this.fileProcessor = fileProcessor;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			if (DirScanner.this.excludeDirs.stream().anyMatch(e -> e.test(dir.toFile()))) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			if (!DirScanner.this.excludeFiles.stream().anyMatch(e -> e.test(file.toFile()))) {
				this.fileProcessor.accept(file.toFile());
			}
			return FileVisitResult.CONTINUE;
		}
	}
}
