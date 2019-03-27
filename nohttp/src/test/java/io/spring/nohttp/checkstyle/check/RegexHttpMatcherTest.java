package io.spring.nohttp.checkstyle.check;

import io.spring.nohttp.HttpMatchResult;
import io.spring.nohttp.RegexHttpMatcher;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class RegexHttpMatcherTest {
	private RegexHttpMatcher matcher = new RegexHttpMatcher();

	@Test
	public void findHttpWhenJustUrlThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("http://example.com");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(0);
	}

	@Test
	public void findHttpWhenUsernameAndPasswordThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://rob:password@example.com");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://rob:password@example.com");
		assertThat(result.getStart()).isEqualTo(4);
	}

	@Test
	public void findHttpWhenStartsWithUrlThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("http://example.com def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(0);
	}

	@Test
	public void findHttpWhenEndsWithUrlThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://example.com");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(4);
	}

	@Test
	public void findHttpWhenDoubleQuotesSurroundThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc \"http://example.com\" def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(5);
	}

	@Test
	public void findHttpWhenSingleQuotesSurroundThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc 'http://example.com' def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(5);
	}

	@Test
	public void findHttpWhenOneHttpAndSingleLineThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://example.com def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(4);
	}

	@Test
	public void findHttpWhenQueryThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://example.com?a=b def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com?a=b");
		assertThat(result.getStart()).isEqualTo(4);
	}

	@Test
	public void findHttpWhenOneHttpAndMultiLineThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("1223\nabc http://example.com def\n5678");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(9);
	}

	@Test
	public void findHttpWhenMultipleHttpAndSingleLineThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://example.com def http://example.org xyz");
		assertThat(results).hasSize(2);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(4);

		result = results.get(1);
		assertThat(result.getHttpUrl()).isEqualTo("http://example.org");
		assertThat(result.getStart()).isEqualTo(27);
	}

	@Test
	public void findHttpWhenHttpsThenNotFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc https://example.com def");
		assertThat(results).isEmpty();
	}

	@Test
	public void findHttpWhenNoUrlThenNotFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc def");
		assertThat(results).isEmpty();
	}
}