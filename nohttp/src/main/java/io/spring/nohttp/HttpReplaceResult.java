package io.spring.nohttp;

import java.util.List;

/**
 * @author Rob Winch
 */
public class HttpReplaceResult {
	private final List<HttpMatchResult> matches;

	private final String result;

	public HttpReplaceResult(List<HttpMatchResult> matches, String result) {
		this.matches = matches;
		this.result = result;
	}

	public boolean isReplacement() {
		return !getMatches().isEmpty();
	}

	public List<HttpMatchResult> getMatches() {
		return this.matches;
	}

	public String getResult() {
		return this.result;
	}
}
