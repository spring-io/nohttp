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

import io.spring.nohttp.HttpMatchResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.spring.nohttp.file.FilePredicates.fileHasName;

/**
 * A scanner which finds .gradle files that contain dsl specific references
 * (i.e. mavenCentral() and jcenter()) that use http. In Gradle >= 2.1 it was updated to
 * use https, so only older versions of Gradle are processed.
 *
 * @author Rob Winch
 */
public class GradleHttpDslScanner {

	private final File dir;

	private GradleHttpDslScanner(File dir) {
		if (dir == null) {
			throw new IllegalArgumentException("dir cannot be null");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a valid directory");
		}
		this.dir = dir;
	}

	/**
	 * Create a new instance
	 * @param dir the directory to scan
	 * @return a {@link GradleHttpDslScanner} to use
	 */
	public static GradleHttpDslScanner create(File dir) {
		return new GradleHttpDslScanner(dir);
	}

	/**
	 * Finds all .gradle files in projects using Gradle < 2.1 and allows processing them
	 * with the provided {@link Consumer}
	 * @param file the {@link Consumer} to process any .gradle files that were found
	 */
	public void scan(Consumer<File> file) {
		List<File> gradleRootDirs = findLegacyGradleRootDir(this.dir);
		if (gradleRootDirs.isEmpty()) {
			return;
		}

		for (File gradleRootDir : gradleRootDirs) {
			DirScanner.create(gradleRootDir)
				.excludeFiles(f -> !f.getName().endsWith(".gradle"))
				.scan(file);
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
				return !(wrapperText.contains("/gradle-0") ||
						wrapperText.contains("/gradle-1") ||
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
