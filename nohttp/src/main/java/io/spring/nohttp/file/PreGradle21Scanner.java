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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.spring.nohttp.file.FilePredicates.fileHasName;

/**
 * A scanner which finds .gradle files for Gradle < 2.1. This is useful for finding gradle
 * DLS (i.e. mavenCentral() and jcenter()) that use http since they use http in Gradle <
 * 2.1 and were switched to https after.
 *
 * @author Rob Winch
 * @see io.spring.nohttp.GradleHttpDsl
 */
public class PreGradle21Scanner {

	private final File dir;

	private PreGradle21Scanner(File dir) {
		if (dir == null) {
			throw new IllegalArgumentException("dir cannot be null");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory");
		}
		this.dir = dir;
	}

	/**
	 * Create a new instance
	 * @param dir the directory to scan
	 * @return a {@link PreGradle21Scanner} to use
	 */
	public static PreGradle21Scanner create(File dir) {
		return new PreGradle21Scanner(dir);
	}

	/**
	 * Finds all .gradle files in projects using Gradle < 2.1 and allows processing them
	 * with the provided {@link Consumer}
	 * @param fileProcessor the {@link Consumer} to process any .gradle files that were found
	 */
	public void scan(Consumer<File> fileProcessor) {
		if (fileProcessor == null) {
			throw new IllegalArgumentException("fileProcessor cannot be null");
		}
		List<File> gradleRootDirs = findLegacyGradleRootDir(this.dir);
		if (gradleRootDirs.isEmpty()) {
			return;
		}

		for (File gradleRootDir : gradleRootDirs) {
			DirScanner.create(gradleRootDir)
				.excludeFiles(f -> !f.getName().endsWith(".gradle"))
				.scan(fileProcessor);
		}
	}

	/**
	 * Find the Gradle Root Project directory for any Gradle version < 2.1 by looking in
	 * gradle/wrapper/gradle-wrapper.properties
	 * @param dir the directory to scan
	 * @return a List of Gradle root project directories
	 */
	private List<File> findLegacyGradleRootDir(File dir) {
		List<File> gradleRoots = new ArrayList<>();

		DirScanner.create(dir)
			.excludeFiles(fileHasName("gradle-wrapper.properties").negate())
			.excludeFiles(f -> {
				String wrapperText = FileUtils.readTextFrom(f);
				return !(wrapperText.contains("/gradle-0.") ||
						wrapperText.contains("/gradle-1.") ||
						wrapperText.contains("/gradle-2.0"));
			})
			.scan(gradleWrapperPropertiesFile -> {
				File gradleRootDir = gradleWrapperPropertiesFile.getParentFile()
						.getParentFile().getParentFile();
				gradleRoots
						.add(gradleRootDir);
			});

		return gradleRoots;
	}
}
