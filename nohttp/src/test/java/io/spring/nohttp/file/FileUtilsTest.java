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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class FileUtilsTest {

	@Rule
	public TemporaryFolder temp =  new TemporaryFolder();

	private String text = "This is the text\nwith a newline";

	@Test
	public void readWrite() throws IOException {
		File file = this.temp.newFile();

		FileUtils.writeTextTo(this.text, file);

		assertThat(FileUtils.readTextFrom(file)).isEqualTo(this.text);
	}

	@Test
	public void readTextFromWhenNullThenIllegalArgumentException() {
		assertThatCode(() -> FileUtils.readTextFrom(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("file cannot be null");
	}

	@Test
	public void readTextFromWhenDirThenIllegalArgumentException() {
		assertThatCode(() -> FileUtils.readTextFrom(this.temp.newFolder()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasCauseInstanceOf(IOException.class);
	}

	@Test
	public void writeTextToWhenNullThenIllegalArgumentException() {
		assertThatCode(() -> FileUtils.writeTextTo(this.text, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("file cannot be null");
	}

	@Test
	public void writeTextFromWhenDirThenIllegalArgumentException() {
		assertThatCode(() -> FileUtils.writeTextTo(this.text, this.temp.newFolder()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasCauseInstanceOf(IOException.class);
	}

	@Test
	public void writeTextFromWhenTextNullIllegalArgumentException() {
		assertThatCode(() -> FileUtils.writeTextTo(null, this.temp.newFile()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("text cannot be null");
	}
}