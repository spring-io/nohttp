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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class HttpProcessorTest {
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private MockHttpProcessor processor = new MockHttpProcessor();

	@Test
	public void processFileWhenNullThenIllegalArgumentException() {
		assertThatCode(() -> this.processor.processFile(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("file cannot be null");
	}

	@Test
	public void processFileWhenFolderThenIllegalArgumentException() throws IOException {
		File dir = this.temp.newFolder();
		assertThatCode(() -> this.processor.processFile(dir))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(dir + " must be a valid file (i.e. not a directory)");
	}

	@Test
	public void processFileWhenDoesNotExistIllegalArgumentException() throws IOException {
		File notExists = new File("doesnotexist");
		assertThatCode(() -> this.processor.processFile(notExists))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(notExists + " does not exist");
	}

	@Test
	public void processFileWhenEmptyResultsThenWorks() throws IOException {
		File file = this.temp.newFile();

		assertThat(this.processor.processFile(file)).isEmpty();
		assertThat(this.processor.files).containsOnly(file);
	}

	@Test
	public void processFileWhenHasResultsThenWorks() throws IOException {
		String httpText = "http://example.local";
		this.processor.results.add(new HttpMatchResult(httpText, 0));
		File file = this.temp.newFile();

		assertThat(this.processor.processFile(file)).containsOnlyElementsOf(this.processor.results);
		assertThat(this.processor.files).containsOnly(file);
		assertThat(this.processor.getHttpMatches()).containsOnly(httpText);
	}

	static class MockHttpProcessor extends HttpProcessor {
		private List<File> files = new ArrayList<>();

		private List<HttpMatchResult> results = new ArrayList<>();

		@Override
		protected List<HttpMatchResult> processHttpInFile(File file) {
			this.files.add(file);
			return this.results;
		}
	};

}