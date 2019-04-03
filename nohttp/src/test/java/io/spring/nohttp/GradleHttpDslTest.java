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

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class GradleHttpDslTest {
	private static final String MAVEN_CENTRAL_DSL = "repositories {\n    mavenCentral()\n}";

	private static final String JCENTER_DSL = "repositories {\n    jcenter()\n}";

	private static final String MAVEN_CENTRAL_JCENTER_DSL = "repositories {\n    mavenCentral()\n    jcenter()\n}";

	private HttpReplacer replacer = GradleHttpDsl.createReplacer();

	@Test
	public void replacerWhenNoMatch() {
		HttpReplaceResult result = this.replacer.replaceHttp("a");

		assertThat(result.isReplacement()).isFalse();
		assertThat(result.getMatches()).isEmpty();
		assertThat(result.getResult()).isEqualTo("a");
	}

	@Test
	public void replacerWhenMavenCentralThenFound() {
		HttpReplaceResult result = this.replacer.replaceHttp(MAVEN_CENTRAL_DSL);

		assertThat(result.isReplacement()).isTrue();
		assertThat(result.getMatches()).hasSize(1);
		HttpMatchResult match = result.getMatches().get(0);
		assertThat(match.getHttp()).isEqualTo("mavenCentral()");
		assertThat(match.getStart()).isEqualTo(19);
		assertThat(result.getResult()).isEqualTo("repositories {\n    maven { url 'https://repo.maven.apache.org/maven2/' }\n}");
	}

	@Test
	public void replacerWhenJcenterThenFound() {
		HttpReplaceResult result = this.replacer.replaceHttp(JCENTER_DSL);

		assertThat(result.isReplacement()).isTrue();
		assertThat(result.getMatches()).hasSize(1);
		HttpMatchResult match = result.getMatches().get(0);
		assertThat(match.getHttp()).isEqualTo("jcenter()");
		assertThat(match.getStart()).isEqualTo(19);
		assertThat(result.getResult()).isEqualTo("repositories {\n    maven { url 'https://jcenter.bintray.com/' }\n}");
	}

	@Test
	public void replacerWhenMavenCentralAndJcenterThenFound() {
		HttpReplaceResult result = this.replacer.replaceHttp(MAVEN_CENTRAL_JCENTER_DSL);

		assertThat(result.isReplacement()).isTrue();
		assertThat(result.getMatches()).hasSize(2);
		HttpMatchResult match = result.getMatches().get(0);
		assertThat(match.getHttp()).isEqualTo("mavenCentral()");
		assertThat(match.getStart()).isEqualTo(19);
		match = result.getMatches().get(1);
		assertThat(match.getHttp()).isEqualTo("jcenter()");
		assertThat(match.getStart()).isEqualTo(38);
		assertThat(result.getResult()).isEqualTo("repositories {\n    maven { url 'https://repo.maven.apache.org/maven2/' }\n    maven { url 'https://jcenter.bintray.com/' }\n}");
	}

	private HttpMatcher matcher = GradleHttpDsl.createMatcher();

	@Test
	public void matcherWhenNoMatch() {
		List<HttpMatchResult> results = this.matcher.findHttp("a");

		assertThat(results).isEmpty();
	}

	@Test
	public void matcherWhenMavenCentralThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp(MAVEN_CENTRAL_DSL);

		assertThat(results).hasSize(1);
		HttpMatchResult match = results.get(0);
		assertThat(match.getHttp()).isEqualTo("mavenCentral()");
		assertThat(match.getStart()).isEqualTo(19);
	}

	@Test
	public void matcherWhenJcenterThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp(JCENTER_DSL);

		assertThat(results).hasSize(1);
		HttpMatchResult match = results.get(0);
		assertThat(match.getHttp()).isEqualTo("jcenter()");
		assertThat(match.getStart()).isEqualTo(19);
	}

	@Test
	public void matcherWhenMavenCentralAndJcenterThenFound() {
		List<HttpMatchResult> results = this.matcher.findHttp(MAVEN_CENTRAL_JCENTER_DSL);

		assertThat(results).hasSize(2);
		HttpMatchResult match = results.get(0);
		assertThat(match.getHttp()).isEqualTo("mavenCentral()");
		assertThat(match.getStart()).isEqualTo(19);
		match = results.get(1);
		assertThat(match.getHttp()).isEqualTo("jcenter()");
		assertThat(match.getStart()).isEqualTo(38);
	}
}