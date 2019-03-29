package io.spring.nohttp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rob Winch
 */
public class RegexHttpMatcher implements HttpMatcher {
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

	public void addHttpUrlWhitelist(Predicate<String> whitelist) {
		if (whitelist == null) {
			throw new IllegalArgumentException("whitelist cannot be null");
		}
		this.httpUrlWhitelist = this.httpUrlWhitelist.or(whitelist);
	}
}
