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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class RegexPredicateTest {

	// constructor

	@Test
	public void constructorWhenNullThenIllegalArgumentException() {
		assertThatCode(() -> new RegexPredicate(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("patterns cannot be null");
	}

	@Test
	public void constructorWhenEmptyThenIllegalArgumentException() {
		assertThatCode(() -> new RegexPredicate(Collections.emptyList()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("patterns cannot be empty");
	}

	// test

	@Test
	public void testWhenMatchAllThenMatch() {
		RegexPredicate test = new RegexPredicate(Arrays.asList(Pattern.compile(".*")));

		assertThat(test.test("foo")).isTrue();
	}

	@Test
	public void testWhenNotMatchThenNoMatch() {
		RegexPredicate test = new RegexPredicate(Arrays.asList(Pattern.compile("^$")));

		assertThat(test.test("foo")).isFalse();
	}

	@Test
	public void testWhenNotMatchAndMatchThenNoMatch() {
		RegexPredicate test = new RegexPredicate(Arrays.asList(Pattern.compile("^$"), Pattern.compile(".*")));

		assertThat(test.test("foo")).isTrue();
	}

	// createWhitelistFromPatterns

	@Test
	public void createWhitelistFromPatternsWhenValid() {
		Predicate<String> test = RegexPredicate.createWhitelistFromPatterns(inputStream(".*"));

		assertThat(test.test("foo")).isTrue();
	}

	@Test
	public void createWhitelistFromPatternsWhenCommentThenCommentIgnored() {
		// the first line is an invalid regular expression (invalid repetition), but it is commented out
		Predicate<String> test = RegexPredicate.createWhitelistFromPatterns(inputStream("// {v}\n.*"));

		assertThat(test.test("foo")).isTrue();
	}

	@Test
	public void createWhitelistFromPatternsWhenEmptyLineThenIgnored() {
		// the first line is empty line which should be ignored
		Predicate<String> test = RegexPredicate.createWhitelistFromPatterns(inputStream("\nNO MATCH"));

		assertThat(test.test("")).isFalse();
	}

	@Test
	public void createWhitelistFromPatternsWhenWhitespaceLineThenIgnored() {
		// the first line is whitespace only line which should be ignored
		Predicate<String> test = RegexPredicate.createWhitelistFromPatterns(inputStream(" \nNO MATCH"));

		assertThat(test.test(" ")).isFalse();
	}

	@Test
	public void createWhitelistFromPatternsWhenInvalidRegexThenUsefulException() {
		// the first line is an invalid regular expression invalid repetition
		assertThatCode(() -> RegexPredicate.createWhitelistFromPatterns(inputStream("{v}")))
			.isInstanceOf(PatternSyntaxException.class);
	}

	private static InputStream inputStream(String s) {
		return new ByteArrayInputStream(s.getBytes());
	}

	////
	// default whitelist

	private Predicate<String> whitelist = RegexPredicate.createDefaultUrlWhitelist();

	// xml

	@Test
	public void testWhenDefaultWhitelistAndSpringNameThenWhitelisted() {
		assertWhitelisted("http://www.springframework.org/schema/beans");
	}

	@Test
	public void testWhenDefaultWhitelistAndEscapedColonSpringNameThenWhitelisted() {
		assertWhitelisted("http\\://www.springframework.org/schema/beans");
	}

	@Test
	public void testWhenDefaultWhitelistAndSpringLocationThenNotWhitelisted() {
		assertNotWhitelisted("http://www.springframework.org/schema/beans/spring-beans.xsd");
	}

	@Test
	public void testWhenDefaultWhitelistAndSpringDtdThenNotWhitelisted() {
		assertNotWhitelisted("http://www.springframework.org/dtd/spring-beans-2.0.dtd");
	}
	
	@Test
	public void testDefaultWehnW3XMLThenWhitelisted() {
		assertWhitelisted("http://www.w3.org/1999/xhtml");
		assertWhitelisted("http://www.w3.org/2001/XMLSchema-datatypes");
		assertWhitelisted("http://www.w3.org/2004/08/xop/include");
	}

	// examples

	@Test
	public void testWhenDefaultWhitelistAndExampleThenNotWhitelisted() {
		assertNotWhitelisted("http://example.com");
	}

	@Test
	public void testWhenDefaultWhitelistAndLocalhostThenWhitelisted() {
		assertWhitelisted("http://localhost");
	}

	@Test
	public void testWhenNoDotThenWhitelisted() {
		assertThat(this.whitelist.test("http://foo")).isTrue();
	}

	// https://tools.ietf.org/html/rfc2606

	@Test
	public void findTestTldEscapedColonIsWhitelisted() {
		assertWhitelisted("http\\://foo.test");
	}

	@Test
	public void findTestTldIsWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("foo.test");
	}

	@Test
	public void findExampleTldIsWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("foo.example");
	}

	@Test
	public void findInvalidTldIsWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("foo.invalid");
	}

	@Test
	public void findLocalhostTldIsWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("foo.localhost");
	}

	public void assertDomainAndSubdomainsWhitelisted(String domain) {
		assertWhitelisted("http://" + domain);
		assertWhitelisted("http://" + domain + "/");
		assertWhitelisted("http://" + domain + "/a/b");
		assertNotWhitelisted("http://example.com/" + domain);
	}

	private void assertWhitelisted(String url) {
		assertThat(this.whitelist.test(url)).describedAs("Should be whitelisted and is not").isTrue();
	}

	private void assertNotWhitelisted(String url) {
		assertThat(this.whitelist.test(url)).describedAs("Should not be whitelisted and is").isFalse();
	}

}