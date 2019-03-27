package io.spring.nohttp;

import java.util.List;

/**
 * @author Rob Winch
 */
public interface HttpMatcher {
	List<HttpMatchResult> findHttp(String text);
}
