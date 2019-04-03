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
 * Allows finding and replacing http matches via a {@link Pattern}
 * @author Rob Winch
 * @see RegexPredicate
 */
public class RegexHttpMatcher implements HttpMatcher, HttpReplacer {
	private Pattern pattern = Pattern.compile("\\b(http\\\\?://[-a-zA-Z0-9+&@/%?=~_|!:,.;]*[-a-zA-Z0-9+&@/%=~_|])");

	private Function<String, String> httpReplacer = httpUrl -> httpUrl.replaceFirst("http", "https");

	private Predicate<String> whitelist;

	/**
	 * Creates a new instance with the provided whitelist
	 * @param whitelist the whitelist to be used
	 */
	public RegexHttpMatcher(Predicate<String> whitelist) {
		if (whitelist == null) {
			throw new IllegalArgumentException("whitelist cannot be null");
		}
		this.whitelist = whitelist;
	}

	/**
	 * Sets the {@link Pattern} used to find http results. The default finds URLs that use http://
	 * @param pattern the pattern to use
	 */
	public void setPattern(Pattern pattern) {
		if (pattern == null) {
			throw new IllegalArgumentException("pattern cannot be null");
		}
		this.pattern = pattern;
	}

	/**
	 * The {@link Function} to use to replace the matched http result with something that
	 * uses https
	 * @param httpReplacer the replacer to use
	 */
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
			if (this.whitelist.test(httpUrl)) {
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

	/**
	 * Adds an additional whitelist to the existing whitelist
	 * @param whitelist the whitelist to use
	 */
	public void addHttpWhitelist(Predicate<String> whitelist) {
		if (whitelist == null) {
			throw new IllegalArgumentException("whitelist cannot be null");
		}
		this.whitelist = this.whitelist.or(whitelist);
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
