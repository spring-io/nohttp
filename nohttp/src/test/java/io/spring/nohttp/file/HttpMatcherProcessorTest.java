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

package io.spring.nohttp.file;

import io.spring.nohttp.HttpMatchResult;
import io.spring.nohttp.HttpMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Rob Winch
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpMatcherProcessorTest {
	private static final File SOURCES_DIR = new File("src/test/resources/file/httpmatcherprocessor");

	@Mock
	private HttpMatcher matcher;

	private HttpMatcherProcessor processor;

	@Before
	public void setup() {
		this.processor = new HttpMatcherProcessor(this.matcher);
	}

	@Test
	public void constructorWhenNullMatcherThenIllegalArgumentException() {
		this.matcher = null;
		assertThatCode(() -> new HttpMatcherProcessor(this.matcher))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("matcher cannot be null");
	}

	@Test
	public void processHttpInFileWhenFoundThenFinds() {
		String httpText = "http://example.local";
		List<HttpMatchResult> results = Arrays.asList(new HttpMatchResult(httpText, 0));
		File file = new File(SOURCES_DIR, "has-http.txt");
		when(this.matcher.findHttp(any())).thenReturn(results);

		assertThat(this.processor.processFile(file)).isEqualTo(results);
		assertThat(this.processor.getHttpMatches()).containsOnly(httpText);
		verify(this.matcher).findHttp("has http://foo.example/a/b/c http content");
	}

	@Test
	public void processHttpInFileWhenNotFoundThenEmpty() {
		List<HttpMatchResult> results = Collections.emptyList();
		File file = new File(SOURCES_DIR, "has-http.txt");
		when(this.matcher.findHttp(any())).thenReturn(results);

		assertThat(this.processor.processFile(file)).isEmpty();
		assertThat(this.processor.getHttpMatches()).isEmpty();
		verify(this.matcher).findHttp("has http://foo.example/a/b/c http content");
	}
}