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
import io.spring.nohttp.RegexHttpMatcher;
import io.spring.nohttp.RegexPredicate;
import io.spring.nohttp.file.DirScanner;
import io.spring.nohttp.file.PreGradle21Scanner;
import io.spring.nohttp.file.HttpMatcherProcessor;
import io.spring.nohttp.file.HttpReplacerProcessor;
import io.spring.nohttp.file.HttpProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author Rob Winch
 */
@Component
@CommandLine.Command(name = "nohttp", mixinStandardHelpOptions = true)
public class ReplaceFilesRunner implements CommandLineRunner, Callable<Void> {
	private InputStream whitelistExclusions;

	@CommandLine.Option(names = "-T", description = "Disable searching only text based files. This is determined using native invocation of grep which will not work on all systems, so it can be disabled.", defaultValue = "true")
	private boolean textFilesOnly = true;

	@CommandLine.Option(names = "-r", description = "Enables replacing the values that were found. The default is to just find the results.", defaultValue = "false")
	private boolean replace = false;

	@CommandLine.Parameters(index = "0", description = "The directory to scan. Default is current working directory.", arity = "0..1")
	private File dir = new File(System.getProperty("user.dir"));

	@CommandLine.Option(names = "-D", paramLabel = "<regex>", description = "Regular expression of directories to exclude scanning. Specify multiple times to provide multiple exclusions. Default is to exclude .git")
	private List<Pattern> dirExclusions = Arrays.asList(Pattern.compile(Pattern.quote(".git")));

	@CommandLine.Option(names = "-F", paramLabel = "<regex>", description = "Regular expression of files to exclude scanning. Specify multiple times to provide multiple exclusions. Default is no file exclusions.")
	private List<Pattern> fileExclusions = new ArrayList<>();

	@CommandLine.Option(names = "-M", description = "Disables printing the matches for specific files.", defaultValue = "false")
	private boolean disablePrintMatches;


	@CommandLine.Option(names = "-f", description = "If true, prints out the file names.", defaultValue = "false")
	private boolean printFiles;

	@CommandLine.Option(names = "-w", description = "The path to file that contains additional whitelist of allowed URLs. The format is a regular expression to whitelist (ignore http URLs) per line.")
	public void setWhitelistFile(File whitelistFile) throws FileNotFoundException {
		this.whitelistExclusions = new FileInputStream(whitelistFile);
	}

	@Override
	public void run(String... args) throws Exception {
		CommandLine.call(this, args);
	}

	@Override
	public Void call() throws Exception {
		RegexHttpMatcher matcher = createMatcher();

		HttpProcessor processor = createHttpProcessor(matcher);

		DirScanner.create(this.dir)
			.textFiles(this.textFilesOnly)
			.excludeDirs(dirExclusions())
			.excludeFiles(fileExclusions())
			.scan(withHttpProcessor(processor));

		writeSummaryReport(processor.getHttpMatches());

		System.out.println();
		System.out.println("Looking for old Gradle DSLs that use http");
		System.out.println();
		RegexHttpMatcher gradleMatcher = RegexHttpMatcher.createGradleDslMatcher();
		HttpProcessor gradleProcessor = createHttpProcessor(gradleMatcher);
		PreGradle21Scanner gradleScanner = PreGradle21Scanner.create(this.dir);

		gradleScanner.scan(withHttpProcessor(gradleProcessor));

		writeSummaryReport(gradleProcessor.getHttpMatches());

		System.out.println("Done!");
		return null;
	}

	private HttpProcessor createHttpProcessor(RegexHttpMatcher matcher) {
		return isReplace() ?
				new HttpReplacerProcessor(matcher) :
				new HttpMatcherProcessor(matcher);
	}

	private Consumer<File> withHttpProcessor(HttpProcessor processor) {
		return file -> {

			List<HttpMatchResult> results = processor.processFile(file);

			if ((!this.disablePrintMatches && !results.isEmpty()) || this.printFiles) {
				System.out.println("Processing " + file);
			}
			if (!this.disablePrintMatches) {
				results.forEach(r -> {
					System.out.println("* Found " + r.getHttp());
				});
			}

		};
	}

	private void writeSummaryReport(Collection<String> httpUrls) {
		System.out.println("");
		if (isReplace()) {
			System.out.println("The Following HTTP results were replaced");
		} else {
			System.out.println("The Following HTTP results were found");
		}
		System.out.println("");
		for (String httpUrl : httpUrls) {
			System.out.println("* " + httpUrl);
		}
		System.out.println("");
		System.out.println("");
	}

	private boolean isReplace() {
		return this.replace;
	}

	private Predicate<File> dirExclusions() {
		return f -> this.dirExclusions.stream().anyMatch(pattern -> pattern.asPredicate().test(f.getName()));
	}

	private Predicate<File> fileExclusions() {
		return f -> this.fileExclusions.stream().anyMatch(pattern -> pattern.asPredicate().test(f.getName()));
	}

	private RegexHttpMatcher createMatcher() {

		RegexHttpMatcher matcher = new RegexHttpMatcher();
		if (this.whitelistExclusions != null) {
			matcher.addHttpUrlWhitelist(RegexPredicate.createWhitelist(this.whitelistExclusions));
		}
		return matcher;
	}
}
