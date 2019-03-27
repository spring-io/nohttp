package io.spring.nohttp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rob Winch
 */
public class RegexHttpMatcher implements HttpMatcher {
	private Pattern pattern = Pattern.compile("\\b(http://[-a-zA-Z0-9+&@/%?=~_|!:,.;]*[-a-zA-Z0-9+&@/%=~_|])");

	public List<HttpMatchResult> findHttp(String text) {
		Matcher matcher = this.pattern.matcher(text);
		List<HttpMatchResult> results = new ArrayList<>();
		while(matcher.find()) {
			String httpUrl = matcher.group();
			int start = matcher.start();
			results.add(new HttpMatchResult(httpUrl, start));
		}
		return results;
	}
}
