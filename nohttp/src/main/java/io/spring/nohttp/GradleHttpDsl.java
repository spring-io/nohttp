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

import java.util.regex.Pattern;

/**
 * Utility for creating {@link HttpReplacer} and {@link HttpMatcher} that finds Gradle's
 * DLSs that use http (i.e. mavenCentral() and jcenter()).
 *
 * Since the API is not aware of external files, neither the {@link HttpReplacer} nor the
 * {@link HttpMatcher} are aware of what Gradle version is being used, so users should
 * first detect that (i.e. using {@link io.spring.nohttp.file.PreGradle21Scanner})
 *
 * @author Rob Winch
 */
public abstract class GradleHttpDsl {

	/**
	 * Creates an {@link HttpReplacer} that finds Gradle's
	 *  DLSs that use http (i.e. mavenCentral() and jcenter()).
	 * @return matcher to use
	 */
	public static HttpReplacer createReplacer() {
		return createGradleDslMatcher();
	}

	/**
	 * Creates an {@link HttpReplacer} that replaces Gradle's
	 *  DLSs that use http (i.e. mavenCentral() and jcenter()).
	 * @return the replacer to use
	 */
	public static HttpMatcher createMatcher() {
		return createGradleDslMatcher();
	}

	private static RegexHttpMatcher createGradleDslMatcher() {
		RegexHttpMatcher matcher = new RegexHttpMatcher(h -> false);
		matcher.setPattern(Pattern.compile("(mavenCentral\\(\\)|jcenter\\(\\))"));
		matcher.setHttpReplacer(http -> {
			if (http.equals("mavenCentral()")) {
				return "maven { url 'https://repo.maven.apache.org/maven2/' }";
			} else if (http.equals("jcenter()")) {
				return "maven { url 'https://jcenter.bintray.com/' }";
			} else {
				throw new IllegalArgumentException("Expected either mavenCentral() or jcenter() but got '" + http + "'");
			}
		});
		return matcher;
	}

	private GradleHttpDsl() {}
}
