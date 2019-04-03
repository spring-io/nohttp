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

import io.spring.nohttp.file.HttpMatcherProcessor;
import io.spring.nohttp.file.HttpProcessor;
import io.spring.nohttp.file.HttpReplacerProcessor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rob Winch
 * @see RegexPredicate
 */
public class RegexHttpMatcher implements HttpMatcher, HttpReplacer {
	private Pattern pattern = Pattern.compile("\\b(http\\\\?://[-a-zA-Z0-9+&@/%?=~_|!:,.;]*[-a-zA-Z0-9+&@/%=~_|])");

	private Function<String, String> httpReplacer = httpUrl -> httpUrl.replaceFirst("http", "https");

	private Predicate<String> httpUrlWhitelist;

	public RegexHttpMatcher() {
		this(RegexPredicate.createDefaultWhitelist());
	}

	public RegexHttpMatcher(Predicate<String> httpUrlWhitelist) {
		if (httpUrlWhitelist == null) {
			throw new IllegalArgumentException("httpUrlWhitelist cannot be null");
		}
		this.httpUrlWhitelist = httpUrlWhitelist;
	}

	public void setPattern(Pattern pattern) {
		if (pattern == null) {
			throw new IllegalArgumentException("pattern cannot be null");
		}
		this.pattern = pattern;
	}

	public void setHttpReplacer(Function<String, String> httpReplacer) {
		if (httpReplacer == null) {
			throw new IllegalArgumentException("httpReplacer cannot be null");
		}
		this.httpReplacer = httpReplacer;
	}

	public List<HttpMatchResult> findHttp(String text) {
		return replaceHttp(text, NoOpWriter.INSTANCE).getMatches();
	}

	public HttpReplaceResult replaceHttp(String text) {
		Writer writer = new StringWriter();
		return replaceHttp(text, writer);
	}

	private HttpReplaceResult replaceHttp(String text, Writer writer) {
		Matcher matcher = this.pattern.matcher(text);
		int currentStart = 0;
		int length = text.length();
		List<HttpMatchResult> results = new ArrayList<>();
		while(matcher.find()) {
			if (currentStart >= length) {
				break;
			}
			String httpUrl = matcher.group();
			if (this.httpUrlWhitelist.test(httpUrl)) {
				continue;
			}
			try {
				writer.append(text, currentStart, matcher.start());
				writer.append(this.httpReplacer.apply(httpUrl));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			currentStart = matcher.end();
			results.add(new HttpMatchResult(httpUrl, matcher.start()));
		}

		if (currentStart < length) {
			try {
				writer.append(text, currentStart, length);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return new HttpReplaceResult(results, writer.toString());
	}

	public void addHttpUrlWhitelist(Predicate<String> whitelist) {
		if (whitelist == null) {
			throw new IllegalArgumentException("whitelist cannot be null");
		}
		this.httpUrlWhitelist = this.httpUrlWhitelist.or(whitelist);
	}

	public static RegexHttpMatcher createGradleDslMatcher() {
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

	private static class NoOpWriter extends Writer {
		public static final NoOpWriter INSTANCE = new NoOpWriter();

		private NoOpWriter() {
		}

		public Writer append(char c) {
			return this;
		}

		public Writer append(CharSequence csq, int start, int end) {
			return this;
		}

		public Writer append(CharSequence csq) {
			return this;
		}

		public void write(int idx) {
		}

		public void write(char[] chr) {
		}

		public void write(char[] chr, int st, int end) {
		}

		public void write(String str) {
		}

		public void write(String str, int st, int end) {
		}

		public void flush() {
		}

		public void close() {
		}
	}
}
