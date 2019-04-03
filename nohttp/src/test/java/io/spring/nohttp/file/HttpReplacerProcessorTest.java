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
import io.spring.nohttp.HttpReplaceResult;
import io.spring.nohttp.HttpReplacer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Rob Winch
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpReplacerProcessorTest {
	@Rule
	public TemporaryFolder temp =  new TemporaryFolder();

	@Mock
	private HttpReplacer replacer;

	private HttpReplacerProcessor processor;

	private File file;

	@Before
	public void setup() throws IOException {
		this.processor = new HttpReplacerProcessor(this.replacer);

		this.file = this.temp.newFile();
		FileUtils.writeTextTo("has http://foo.example/a/b/c http content", this.file);
	}

	@Test
	public void constructorWhenNullMatcherThenIllegalArgumentException() {
		this.replacer = null;
		assertThatCode(() -> new HttpReplacerProcessor(this.replacer))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("replacer cannot be null");
	}

	@Test
	public void processHttpInFileWhenFoundThenFinds() {
		String httpText = "http://example.local";
		List<HttpMatchResult> results = Arrays.asList(new HttpMatchResult(httpText, 0));
		HttpReplaceResult result = new HttpReplaceResult(results, "Replaced");
		when(this.replacer.replaceHttp(any())).thenReturn(result);

		assertThat(this.processor.processFile(this.file)).isEqualTo(result.getMatches());
		assertThat(this.processor.getHttpMatches()).containsOnly(httpText);
		assertThat(FileUtils.readTextFrom(this.file)).isEqualTo(result.getResult());
		verify(this.replacer).replaceHttp("has http://foo.example/a/b/c http content");
	}
}