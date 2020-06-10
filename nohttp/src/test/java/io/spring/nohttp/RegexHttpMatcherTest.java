/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.nohttp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Rob Winch
 */
@RunWith(MockitoJUnitRunner.class)
public class RegexHttpMatcherTest {
	@Mock
	private Predicate<String> allow;

	private RegexHttpMatcher matcher;

	@Before
	public void setup() {
		this.matcher = new RegexHttpMatcher(this.allow);
	}

	// constructor

	@Test
	public void constructorWhenNullPredicateThenIllegalArgumentException() {
		this.allow = null;
		assertThatCode(() -> new RegexHttpMatcher(this.allow))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("allow cannot be null");
	}

	// setPattern

	@Test
	public void setPatternWhenNullThenIllegalArgumentException() {
		Pattern pattern = null;
		assertThatCode(() -> this.matcher.setPattern(pattern))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("pattern cannot be null");
	}

	// setReplacer

	@Test
	public void setReplacerWhenNullThenIllegalArgumentException() {
		Function<String, String> replacer = null;
		assertThatCode(() -> this.matcher.setHttpReplacer(replacer))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("httpReplacer cannot be null");
	}

	// addHttpUrlAllowlist

	@Test
	public void addHttpUrlAllowlistWhenNullThenIllegalArgumentException() {
		Predicate<String> allow = null;
		assertThatCode(() -> this.matcher.addHttpAllow(allow))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("allow cannot be null");
	}

	// findHttp

	@Test
	public void findHttpWhenCustomPatternThenFound() {
		Pattern pattern = Pattern.compile("a");
		this.matcher.setPattern(pattern);
		this.matcher.setHttpReplacer(h -> h.toUpperCase());

		List<HttpMatchResult> results = this.matcher.findHttp("123a456");

		assertThat(results).hasSize(1);
		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("a");
		assertThat(result.getStart()).isEqualTo(3);
	}

	@Test
	public void findWhenAddAllowlistThenAllowed() {
		this.matcher = new RegexHttpMatcher(http -> false);
		this.matcher.addHttpAllow(http -> true);
		List<HttpMatchResult> results = this.matcher.findHttp("http://example.com");
		assertThat(results).isEmpty();
	}

	// findHttp default pattern testing

	/**
	 * for .properties files
 	 */
	@Test
	public void findWhenHttpEscapleColonThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("http\\://example.com");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http\\://example.com");
		assertThat(result.getStart()).isEqualTo(0);
	}

	@Test
	public void findWhenHttpsThenNotFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("https://example.com");
		assertThat(results).isEmpty();
	}

	@Test
	public void findWhenAllowedThenNotFound() {
		String url = "http://example.com";
		when(this.allow.test(url)).thenReturn(true);
		List<HttpMatchResult> results = this.matcher.findHttp(url);
		assertThat(results).isEmpty();
	}

	@Test
	public void findHttpWhenJustUrlThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("http://example.com");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(0);
	}

	@Test
	public void findHttpWhenUsernameAndPasswordThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://rob:password@example.com");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://rob:password@example.com");
		assertThat(result.getStart()).isEqualTo(4);
	}

	@Test
	public void findHttpWhenStartsWithUrlThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("http://example.com def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(0);
	}

	@Test
	public void findHttpWhenEndsWithUrlThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://example.com");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(4);
	}

	@Test
	public void findHttpWhenDoubleQuotesSurroundThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc \"http://example.com\" def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(5);
	}

	@Test
	public void findHttpWhenSingleQuotesSurroundThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc 'http://example.com' def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(5);
	}

	@Test
	public void findHttpWhenOneHttpAndSingleLineThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://example.com def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(4);
	}

	@Test
	public void findHttpWhenQueryThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://example.com?a=b def");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com?a=b");
		assertThat(result.getStart()).isEqualTo(4);
	}

	@Test
	public void findHttpWhenOneHttpAndMultiLineThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("1223\nabc http://example.com def\n5678");
		assertThat(results).hasSize(1);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(9);
	}

	@Test
	public void findHttpWhenMultipleHttpAndSingleLineThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp("abc http://example.com def http://example.org xyz");
		assertThat(results).hasSize(2);

		HttpMatchResult result = results.get(0);
		assertThat(result.getHttp()).isEqualTo("http://example.com");
		assertThat(result.getStart()).isEqualTo(4);

		result = results.get(1);
		assertThat(result.getHttp()).isEqualTo("http://example.org");
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

	// replaceHttp default pattern testing

	@Test
	public void replaceHttpWhenJustUrlThenHttps() {
		assertReplaceHttpEquals("http://a.example", "https://a.example");
	}

	@Test
	public void replaceHttpWhenMultilineJustUrlsThenHttps() {
		assertReplaceHttpEquals("http://a.example\nhttp://b.test", "https://a.example\nhttps://b.test");
	}

	@Test
	public void replaceHttpWhenSingleUrlThenHttps() {
		assertReplaceHttpEquals("abc http://a.example def", "abc https://a.example def");
	}

	@Test
	public void replaceHttpWhenMultilineThenHttps() {
		assertReplaceHttpEquals("abc http://a.example def\nghi http://b.test jkl", "abc https://a.example def\nghi https://b.test jkl");
	}

	@Test
	public void replaceHttpWhenSingleUrlStartOnlyThenHttps() {
		assertReplaceHttpEquals("abc http://a.example", "abc https://a.example");
	}

	@Test
	public void replaceHttpWhenSingleUrlEndOnlyThenHttps() {
		assertReplaceHttpEquals("http://a.example def", "https://a.example def");
	}

	@Test
	public void replaceHttpWhenMultiUrlThenHttps() {
		assertReplaceHttpEquals("abc http://a.example def http://b.test ghi", "abc https://a.example def https://b.test ghi");
	}

	// replaceHttp

	@Test
	public void replaceHttpWhenCustomPatternAndReplacerThenFound() {
		Pattern pattern = Pattern.compile("a");
		this.matcher.setPattern(pattern);
		this.matcher.setHttpReplacer(http -> http.toUpperCase());

		HttpReplaceResult result = this.matcher.replaceHttp("123a456");

		assertThat(result.isReplacement()).isTrue();
		assertThat(result.getResult()).isEqualTo("123A456");
		assertThat(result.getMatches()).hasSize(1);
		HttpMatchResult match = result.getMatches().get(0);
		assertThat(match.getHttp()).isEqualTo("a");
		assertThat(match.getStart()).isEqualTo(3);
	}

	@Test
	public void replaceHttpWhenAddAllowlistThenAllowed() {
		this.matcher = new RegexHttpMatcher(http -> false);
		this.matcher.addHttpAllow(http -> true);
		HttpReplaceResult result = this.matcher
				.replaceHttp("http://example.com");
		assertThat(result.isReplacement()).isFalse();
		assertThat(result.getResult()).isEqualTo("http://example.com");
	}

	@Test
	public void replaceHttpWhenNotReplacedThenAllowed() {
		this.matcher = new RegexHttpMatcher(http -> false);
		this.matcher.setHttpReplacer(u -> u);
		HttpReplaceResult result = this.matcher
				.replaceHttp("http://example.com");
		assertThat(result.isReplacement()).isFalse();
		assertThat(result.getResult()).isEqualTo("http://example.com");
	}

	private HttpReplaceResult assertReplaceHttpEquals(String text, String result) {
		HttpReplaceResult matches = this.matcher.replaceHttp(text);
		assertThat(matches.getResult()).isEqualTo(result);
		return matches;
	}
}