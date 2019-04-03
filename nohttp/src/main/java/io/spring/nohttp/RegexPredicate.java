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

package io.spring.nohttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An API that is typically used with {@link RegexHttpMatcher} that allows whitelisting
 * URLs using a provided list of {@link Pattern}s.
 *
 * @author Rob Winch
 * @see RegexHttpMatcher
 * @see #createDefaultWhitelistPatterns(InputStream)
 */
public class RegexPredicate implements Predicate<String> {
	private final List<Pattern> patterns;

	/**
	 * Creates a whitelist with the provided {@link Pattern}s
	 * @param patterns the patterns to use.
	 */
	public RegexPredicate(List<Pattern> patterns) {
		if (patterns == null || patterns.isEmpty()) {
			throw new IllegalArgumentException("patterns cannot be null or empty. Got " + patterns);
		}
		this.patterns = patterns;
	}

	@Override
	public boolean test(String httpUrl) {
		return this.patterns.stream()
				.anyMatch(p -> p.matcher(httpUrl).matches());
	}

	/**
	 * Creates an instance that uses the default whitelist. The whitelist is expected to
	 * be updated in upcoming releases, but generally contains
	 *
	 * <ul>
	 *     <li>localhost</li>
	 *     <li>URLs that use a TLD defined in https://tools.ietf.org/html/rfc2606 (i.e. tld of test, .example, invalid, localhost)</li>
	 *     <li>XML Namespace names (not the locations)</li>
	 *     <li>Java specific URLs that do not work over http. For example, Java Properties
	 *     <a href="https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/43ca3768126e/src/share/classes/sun/util/xml/PlatformXmlPropertiesProvider.java#l198">hard codes</a> using http.
	 *     </li>
	 * </ul>
	 * @return
	 */
	public static Predicate<String> createDefaultWhitelist() {
		InputStream resource = RegexPredicate.class.getResourceAsStream("whitelist.txt");
		return createWhitelist(resource);
	}

	/**
	 * Allows creating a instance of {@link RegexHttpMatcher} from an {@link InputStream}.
	 * The format of the {@link InputStream} contains regular expressions of what URLs
	 * should be whitelisted such that:
	 *
	 * <ul>
	 *     <li>Each line contains a regular expression that should be whitelisted</li>
	 *     <li>Lines can begin with // to create a comment within the file</li>
	 *     <li>Lines are trimmed for whitespace</li>
	 *     <li>Lines that are empty are ignored</li>
	 * </ul>
	 *
	 * An example file can be found below:
	 *
	 * <pre>
	 * // Ignore Maven XML Namespace id of http://maven.apache.org/POM/4.0.0
	 * ^http://maven\.apache\.org/POM/4.0.0$
	 * // Whitelist Company XML namespace names but not the locations (which end in .xsd)
	 * ^http://mycompany.test/xml/.*(?<!\.(xsd))$
	 * </pre>
	 * @param resource
	 * @return
	 */
	public static Predicate<String> createWhitelist(InputStream resource) {
		List<Pattern> patterns = createDefaultWhitelistPatterns(resource);
		return httpUrl -> patterns.stream()
				.anyMatch(p -> p.matcher(httpUrl).matches());
	}

	private static List<Pattern> createDefaultWhitelistPatterns(InputStream resource) {
		if (resource == null) {
			throw new IllegalStateException("Failed to load whitelist from " + resource);
		}
		InputStreamReader input = new InputStreamReader(resource);

		try (BufferedReader reader = new BufferedReader(input)) {
			return reader.lines()
					.map(String::trim)
					.filter(l -> !l.startsWith("//"))
					.filter(l -> l.length() != 0)
					.map(Pattern::compile)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}
}
