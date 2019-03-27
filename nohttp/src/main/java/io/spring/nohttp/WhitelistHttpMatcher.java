package io.spring.nohttp;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rob Winch
 */
public class WhitelistHttpMatcher implements HttpMatcher {
	private final HttpMatcher delegate;

	public WhitelistHttpMatcher() {
		this(new RegexHttpMatcher());
	}

	WhitelistHttpMatcher(HttpMatcher delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("delegate cannot be null");
		}
		this.delegate = delegate;
	}

	@Override
	public List<HttpMatchResult> findHttp(String text) {
		return this.delegate.findHttp(text)
			.stream()
			.filter(this::isNotWhitelisted)
			.collect(Collectors.toList());
	}

	private boolean isNotWhitelisted(HttpMatchResult result) {
		String httpUrl = result.getHttpUrl();
		return ! isWhitelisted(httpUrl);
	}

	private boolean isWhitelisted(String httpUrl) {
		return httpUrl.startsWith("http://localhost");
	}
}
