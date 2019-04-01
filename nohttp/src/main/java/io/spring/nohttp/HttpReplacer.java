package io.spring.nohttp;

/**
 * @author Rob Winch
 */
public interface HttpReplacer {
	HttpReplaceResult replaceHttp(String text);
}
