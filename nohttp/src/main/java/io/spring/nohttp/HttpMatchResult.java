package io.spring.nohttp;

/**
 * @author Rob Winch
 */
public class HttpMatchResult {
	private final String httpUrl;

	private final int start;

	public HttpMatchResult(String httpUrl, int start) {
		this.httpUrl = httpUrl;
		this.start = start;
	}

	public String getHttpUrl() {
		return this.httpUrl;
	}

	public int getStart() {
		return this.start;
	}
}
