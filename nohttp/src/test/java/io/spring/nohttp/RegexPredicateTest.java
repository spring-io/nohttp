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

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class RegexPredicateTest {

	private Predicate<String> whitelist = RegexPredicate.createDefaultWhitelist();

	// xml
	
	@Test
	public void testWhenDefaultWhitelistAndSpringNameThenWhitelisted() {
		assertWhitelisted("http://www.springframework.org/schema/beans");
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