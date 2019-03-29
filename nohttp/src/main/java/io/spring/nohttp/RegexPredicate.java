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
 * @author Rob Winch
 */
public class RegexPredicate implements Predicate<String> {
	private final List<Pattern> patterns;

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

	static Predicate<String> createDefaultWhitelist() {
		InputStream resource = RegexPredicate.class.getResourceAsStream("whitelist.txt");
		return createWhitelist(resource);
	}

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
