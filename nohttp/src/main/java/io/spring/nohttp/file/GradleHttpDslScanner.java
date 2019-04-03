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
import io.spring.nohttp.RegexHttpMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author Rob Winch
 */
public class GradleHttpDslScanner {

	private final File dir;

	private GradleHttpDslScanner(File dir) {
		this.dir = dir;
	}

	public static GradleHttpDslScanner create(File file) {
		return new GradleHttpDslScanner(file);
	}

	public void scan(Consumer<File> file) {
		List<File> gradleRootDirs = findLegacyGradleRootDir(this.dir);
		if (gradleRootDirs.isEmpty()) {
			return;
		}

		List<HttpMatchResult> results = new ArrayList<>();
		for (File gradleRootDir : gradleRootDirs) {
			DirScanner.create(gradleRootDir)
				.excludeFiles(f -> !f.getName().endsWith(".gradle"))
				.scan(file);
		}
	}

	private List<File> findLegacyGradleRootDir(File dir) {
		List<File> gradleRoots = new ArrayList<>();

		DirScanner.create(dir)
			.excludeFiles(f -> {
				if(!f.getName().equals("gradle-wrapper.properties")) {
					return true;
				}
				String wrapperText = FileUtils.readText(f);
				return !(wrapperText.contains("/gradle-0") ||
						wrapperText.contains("/gradle-1") ||
						wrapperText.contains("/gradle-2.0"));
			})
			.scan(gradleWrapperPropertiesFile -> gradleRoots.add(gradleWrapperPropertiesFile.getParentFile().getParentFile().getParentFile()));

		return gradleRoots;
	}
}
