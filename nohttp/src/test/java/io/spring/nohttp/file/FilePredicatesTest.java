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
public class FilePredicatesTest {
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void fileHasNameWhenMatchesThenTrue() throws IOException {
		File f = this.temp.newFile();

		assertThat(FilePredicates.fileHasName(f.getName()).test(f)).isTrue();
	}

	@Test
	public void fileHasNameWhenNotMatchesThenTrue() throws IOException {
		File f = this.temp.newFile();

		assertThat(FilePredicates.fileHasName(f.getName() + "NOT").test(f)).isFalse();
	}

	@Test
	public void fileHasNameWhenNameNullThenIllegalArgumentException() throws IOException {
		assertThatCode(() -> FilePredicates.fileHasName(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("fileName cannot be null");
	}
}