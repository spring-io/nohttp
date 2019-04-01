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

package io.spring.nohttp.cli;

import io.spring.nohttp.HttpMatchResult;
import io.spring.nohttp.HttpReplaceResult;
import io.spring.nohttp.HttpReplacer;
import io.spring.nohttp.RegexHttpMatcher;
import io.spring.nohttp.cli.FileUtils.ExcludeDirFileVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Rob Winch
 */
public class ReplaceFilesMain {

	private HttpReplacer matcher = new RegexHttpMatcher();

	public List<HttpMatchResult> replaceTextFilesInDir(File dir) {
		List<File> textFiles = FileUtils.getTextFiles(dir);
		return textFiles.stream()
				.map(this::replaceHttpInFile)
				.map(HttpReplaceResult::getMatches)
				.reduce(new ArrayList<>(), (accum, list) -> {
					accum.addAll(list);
					return accum;
				});
	}

	public void replaceForPaths(String... paths) {
		Stream.of(paths)
			.map(File::new)
			.forEach(file -> {
				if (file.isFile()) {
					replaceHttpInFile(file).getMatches();
				} else {
					replaceHttpInDir(file);
				}
			});
	}

	public void replaceHttpInDir(File dir) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory");
		}

		try {
			Files.walkFileTree(dir.toPath(), new ReplaceHttpFileVisitor());
		}
		catch (IOException e) {
			throw new RuntimeException("Could not process " + dir, e);
		}
	}

	public HttpReplaceResult replaceHttpInFile(File file) {
		System.out.println("Processing " + file);
		String originalText = IOUtils.readText(file);

		HttpReplaceResult result = this.matcher.replaceHttp(originalText);
		if (!result.isReplacement()) {
			return result;
		}

		IOUtils.writeTextTo(result.getResult(), file);
		return result;
	}

	public static void main(String[] args) throws IOException {
		ReplaceFilesMain app = new ReplaceFilesMain();
		if (args.length == 0) {
			File workingDir = new File(System.getProperty("user.dir"));
			List<HttpMatchResult> results = app.replaceTextFilesInDir(workingDir);
			writeReport(results);
		} else {
			app.replaceForPaths(args);
		}
	}

	private static void writeReport(List<HttpMatchResult> results) {
		Set<String> uniqueHttpUrls = results.stream()
				.map(HttpMatchResult::getHttpUrl)
				.collect(Collectors.toSet());
		List<String> httpUrls = new ArrayList<>(uniqueHttpUrls.size());
		httpUrls.addAll(uniqueHttpUrls);
		Collections.sort(httpUrls);
		System.out.println("");
		System.out.println("The Following URLs were replaced");
		System.out.println("");
		for (String httpUrl : httpUrls) {
			System.out.println("Replaced " + httpUrl);
		}
	}

	class ReplaceHttpFileVisitor extends ExcludeDirFileVisitor {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			replaceHttpInFile(file.toFile());
			return FileVisitResult.CONTINUE;
		}
	}
}
