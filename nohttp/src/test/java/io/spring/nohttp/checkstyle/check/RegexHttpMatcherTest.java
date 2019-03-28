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
	private RegexHttpMatcher matcher = new RegexHttpMatcher(url -> false);

	private RegexHttpMatcher whitelisted = new RegexHttpMatcher();

	@Test
	public void findHttpWhenWhitelistSpringBeanSchemaName() {
		List<HttpMatchResult> results = this.whitelisted.findHttp("http://www.springframework.org/schema/beans");
		assertThat(results).isEmpty();
	}

	@Test
	public void findHttpWhenWhitelistSpringBeanSchemaLocation() {
		List<HttpMatchResult> results = this.whitelisted.findHttp("http://www.springframework.org/schema/beans/spring-beans.xsd");
		assertThat(results).hasSize(1);
	}

	@Test
	public void findHttpWhenWhitelistSpringBeanDtdLocation() {
		List<HttpMatchResult> results = this.whitelisted.findHttp("http://www.springframework.org/dtd/spring-beans-2.0.dtd");
		assertThat(results).hasSize(1);
	}

	@Test
	public void findHttpWhenWhitelistAndExampleThenFound() {
		List<HttpMatchResult> results = this.whitelisted.findHttp("http://example.com");
		assertThat(results).hasSize(1);
	}

	@Test
	public void findHttpWhenWhitelistLocalhost() {
		List<HttpMatchResult> results = this.whitelisted.findHttp("http://localhost");
		assertThat(results).isEmpty();
	}

	@Test
	public void findHttpWhenNoDot() {
		List<HttpMatchResult> results = this.whitelisted.findHttp("http://foo");
		assertThat(results).isEmpty();
	}

	@Test
	public void findHttpWhenW3Names() {
		assertWhitelisted("http://www.w3.org/1999/xhtml");
		assertWhitelisted("http://www.w3.org/2001/XMLSchema-datatypes");
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

	// no https

	@Test
	public void findHttpWhenSourceForge() {
		assertWhitelisted(" http://iharder.sourceforge.net/current/java/base64/");
	}

	@Test
	public void findHttpWhenOpensecurityResearchThenWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("blog.opensecurityresearch.com");
		assertDomainAndSubdomainsWhitelisted("opensecurityresearch.com");
	}

	@Test
	public void findHttpWhenWebappsecThenWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("lists.webappsec.org");
		assertDomainAndSubdomainsWhitelisted("webappsec.org");
	}

	@Test
	public void findHttpWhenJaspanThenWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("jaspan.com");
	}

	@Test
	public void findHttpWhenCsBerkelyThenWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("cs.berkeley.edu");
		assertDomainAndSubdomainsWhitelisted("webblaze.cs.berkeley.edu");
	}

	@Test
	public void findHttpWhenNabbleThenWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("bouncy-castle.1462172.n4.nabble.com");
		assertDomainAndSubdomainsWhitelisted("nabble.com");
	}

	@Test
	public void findHttpWhenZytraxThenWhitelisted() {
		assertDomainAndSubdomainsWhitelisted("www.zytrax.com");
		assertDomainAndSubdomainsWhitelisted("zytrax.com");
	}

	public void assertDomainAndSubdomainsWhitelisted(String domain) {
		assertWhitelisted("http://" + domain);
		assertWhitelisted("http://" + domain + "/");
		assertWhitelisted("http://" + domain + "/a/b");
		assertNotWhitelisted("http://example.com/" + domain);
	}

	@Test
	public void findExampleWhitelisted() {
		assertWhitelisted("http://foo.example");
	}

	@Test
	public void findHttpWhenNoDotAndSlash() {
		List<HttpMatchResult> results = this.whitelisted.findHttp("http://foo/");
		assertThat(results).isEmpty();
	}

	@Test
	public void findHttpWhenWhitelistAll() {
		RegexHttpMatcher whitelisted = new RegexHttpMatcher(url -> true);
		List<HttpMatchResult> results = whitelisted.findHttp("http://example.com");
		assertThat(results).isEmpty();
	}

	private void assertWhitelisted(String url) {
		List<HttpMatchResult> results = this.whitelisted.findHttp(url);
		assertThat(results).describedAs(url + " should be whitelisted but is not").isEmpty();
	}

	private List<HttpMatchResult> assertNotWhitelisted(String url) {
		List<HttpMatchResult> results = this.whitelisted.findHttp(url);
		assertThat(results).describedAs(url + " should NOT be whitelisted but is").hasSize(1);
		return results;
	}

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