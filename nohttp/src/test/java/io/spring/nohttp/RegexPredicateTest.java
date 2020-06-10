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

	// createAllowlistFromPatterns

	@Test
	public void createAllowlistFromPatternsWhenValid() {
		Predicate<String> test = RegexPredicate.createAllowlistFromPatterns(inputStream(".*"));

		assertThat(test.test("foo")).isTrue();
	}

	@Test
	public void createAllowlistFromPatternsWhenCommentThenCommentIgnored() {
		// the first line is an invalid regular expression (invalid repetition), but it is commented out
		Predicate<String> test = RegexPredicate.createAllowlistFromPatterns(inputStream("// {v}\n.*"));

		assertThat(test.test("foo")).isTrue();
	}

	@Test
	public void createAllowlistFromPatternsWhenEmptyLineThenIgnored() {
		// the first line is empty line which should be ignored
		Predicate<String> test = RegexPredicate.createAllowlistFromPatterns(inputStream("\nNO MATCH"));

		assertThat(test.test("")).isFalse();
	}

	@Test
	public void createAllowlistFromPatternsWhenWhitespaceLineThenIgnored() {
		// the first line is whitespace only line which should be ignored
		Predicate<String> test = RegexPredicate.createAllowlistFromPatterns(inputStream(" \nNO MATCH"));

		assertThat(test.test(" ")).isFalse();
	}

	@Test
	public void createAllowlistFromPatternsWhenInvalidRegexThenUsefulException() {
		// the first line is an invalid regular expression invalid repetition
		assertThatCode(() -> RegexPredicate.createAllowlistFromPatterns(inputStream("{v}")))
			.isInstanceOf(PatternSyntaxException.class);
	}

	private static InputStream inputStream(String s) {
		return new ByteArrayInputStream(s.getBytes());
	}

	////
	// default allowlist

	private Predicate<String> allowlist = RegexPredicate.createDefaultUrlAllowlist();

	// xml

	@Test
	public void testWhenDefaultAllowlistAndSpringNameThenAllowlisted() {
		assertAllowlisted("http://www.springframework.org/schema/beans");
	}

	@Test
	public void testWhenDefaultAllowlistAndEscapedColonSpringNameThenAllowlisted() {
		assertAllowlisted("http\\://www.springframework.org/schema/beans");
	}

	@Test
	public void testWhenDefaultAllowlistAndSpringLocationThenNotAllowlisted() {
		assertNotAllowlisted("http://www.springframework.org/schema/beans/spring-beans.xsd");
	}

	@Test
	public void testWhenDefaultAllowlistAndSpringDtdThenNotAllowlisted() {
		assertNotAllowlisted("http://www.springframework.org/dtd/spring-beans-2.0.dtd");
	}

	@Test
	public void testDefaultWehnW3XMLThenAllowlisted() {
		assertAllowlisted("http://www.w3.org/1999/xhtml");
		assertAllowlisted("http://www.w3.org/2001/XMLSchema-datatypes");
		assertAllowlisted("http://www.w3.org/2004/08/xop/include");
	}

	@Test
	public void testDefaultWhenOpenofficeThenAllowlisted() {
		assertAllowlisted("http://openoffice.org/2000/office");
		assertAllowlisted("http://openoffice.org/2000/meta");
		assertAllowlisted("http://openoffice.org/2000/presentation");
	}

	@Test
	// gh-4
	public void docbookXslthlWhenDefaultAllowlistThenAllowlisted() {
		assertAllowlisted("http://xslthl.sf.net");
	}

	// examples

	@Test
	public void testWhenDefaultAllowlistAndExampleThenNotAllowlisted() {
		assertNotAllowlisted("http://example.com");
	}

	@Test
	public void testWhenDefaultAllowlistAndLocalhostThenAllowlisted() {
		assertAllowlisted("http://localhost");
	}

	@Test
	public void testWhenNoDotThenAllowlisted() {
		assertThat(this.allowlist.test("http://foo")).isTrue();
	}

	// https://tools.ietf.org/html/rfc2606

	@Test
	public void findTestTldEscapedColonIsAllowlisted() {
		assertAllowlisted("http\\://foo.test");
	}

	@Test
	public void findTestTldIsAllowlisted() {
		assertDomainAndSubdomainsAllowlisted("foo.test");
	}

	@Test
	public void findExampleTldIsAllowlisted() {
		assertDomainAndSubdomainsAllowlisted("foo.example");
	}

	@Test
	public void findInvalidTldIsAllowlisted() {
		assertDomainAndSubdomainsAllowlisted("foo.invalid");
	}

	@Test
	public void findLocalhostTldIsAllowlisted() {
		assertDomainAndSubdomainsAllowlisted("foo.localhost");
	}

	public void assertDomainAndSubdomainsAllowlisted(String domain) {
		assertAllowlisted("http://" + domain);
		assertAllowlisted("http://" + domain + "/");
		assertAllowlisted("http://" + domain + "/a/b");
		assertNotAllowlisted("http://example.com/" + domain);
	}

	private void assertAllowlisted(String url) {
		assertThat(this.allowlist.test(url)).describedAs("Should be allowlisted and is not").isTrue();
	}

	private void assertNotAllowlisted(String url) {
		assertThat(this.allowlist.test(url)).describedAs("Should not be allowlisted and is").isFalse();
	}

	@Test
	//gh-28
	public void testJsfTaglibs() {
		assertAllowlisted("http://xmlns.jcp.org/jsf");
		assertAllowlisted("http://xmlns.jcp.org/jsf/html");
		assertAllowlisted("http://xmlns.jcp.org/jsf/core");
		assertAllowlisted("http://xmlns.jcp.org/jsf/facelets");
		assertAllowlisted("http://xmlns.jcp.org/jsf/composite");
		assertAllowlisted("http://xmlns.jcp.org/jsf/composite/foo-bar");
		assertAllowlisted("http://xmlns.jcp.org/jsp/jstl/core");
		assertAllowlisted("http://xmlns.jcp.org/jsp/jstl/functions");
		assertAllowlisted("http://xmlns.jcp.org/jsf/passthrough");
		assertAllowlisted("http://primefaces.org/ui");
		assertAllowlisted("http://bootsfaces.net/ui");
	}

}