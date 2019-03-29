package io.spring.nohttp;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rob Winch
 */
public class RegexHttpMatcher implements HttpMatcher, HttpReplacer {
	private Pattern pattern = Pattern.compile("\\b(http://[-a-zA-Z0-9+&@/%?=~_|!:,.;]*[-a-zA-Z0-9+&@/%=~_|])");

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
				writer.append(httpUrl.replaceFirst("http", "https"));
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
