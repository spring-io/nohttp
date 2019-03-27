package io.spring.nohttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Rob Winch
 */
public class RegexHttpMatcher implements HttpMatcher {
	private Pattern pattern = Pattern.compile("\\b(http://[-a-zA-Z0-9+&@/%?=~_|!:,.;]*[-a-zA-Z0-9+&@/%=~_|])");

	private Predicate<String> httpUrlWhitelist;

	public RegexHttpMatcher() {
		this(createDefaultWhitelist());
	}

	public RegexHttpMatcher(Predicate<String> httpUrlWhitelist) {
		if (httpUrlWhitelist == null) {
			throw new IllegalArgumentException("httpUrlWhitelist cannot be null");
		}
		this.httpUrlWhitelist = httpUrlWhitelist;
	}

	public List<HttpMatchResult> findHttp(String text) {
		Matcher matcher = this.pattern.matcher(text);
		List<HttpMatchResult> results = new ArrayList<>();
		while(matcher.find()) {
			String httpUrl = matcher.group();
			if (this.httpUrlWhitelist.test(httpUrl)) {
				continue;
			}
			int start = matcher.start();
			results.add(new HttpMatchResult(httpUrl, start));
		}
		return results;
	}

	static Predicate<String> createDefaultWhitelist() {
		List<Pattern> patterns = createDefaultWhitelistPatterns();
		return httpUrl -> patterns.stream()
				.anyMatch(p -> p.matcher(httpUrl).matches());
	}

	static List<Pattern> createDefaultWhitelistPatterns() {
		InputStream resource = RegexHttpMatcher.class.getResourceAsStream("whitelist.txt");
		if (resource == null) {
			throw new IllegalStateException("Failed to load default whitelist");
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
