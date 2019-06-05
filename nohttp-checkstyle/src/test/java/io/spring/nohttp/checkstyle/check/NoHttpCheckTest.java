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

package io.spring.nohttp.checkstyle.check;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Rob Winch
 */
public class NoHttpCheckTest {

	private NoHttpCheck check = new NoHttpCheck();

	@Test
	public void setWhitelistFilenameWhenNullThenIllegalArgumentException() {
		assertThatCode(() -> this.check.setWhitelistFileName(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("whitelistFileName cannot be null");
	}

	@Test
	public void setWhitelistWhenNullThenIllegalArgumentException() {
		assertThatCode(() -> this.check.setWhitelist(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("whitelist cannot be null");
	}

	@Test
	public void getExternalResourceLocationsWhenNoWhitelistThenEmpty() {
		assertThat(this.check.getExternalResourceLocations()).isEmpty();
	}

	@Test
	public void getExternalResourceLocationsWhenWhitelistThenEmpty() {
		String whitelistFileName = "whitelist.lines";
		this.check.setWhitelistFileName(whitelistFileName);
		assertThat(this.check.getExternalResourceLocations()).containsOnly(whitelistFileName);
	}
}