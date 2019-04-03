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

/**
 * Represents where an http result which includes the contents (i.e. an http:// URL) and the index (starting at 0) where the result was found.
 * @author Rob Winch
 */
public class HttpMatchResult {
	private final String http;

	private final int start;

	/**
	 * Creates a new instance
	 * @param http the http:// URL that was found
	 * @param start the index (starting at 0) where the http:// URL was found
	 */
	public HttpMatchResult(String http, int start) {
		this.http = http;
		this.start = start;
	}

	/**
	 * The http result that was found (i.e. an http:// URL)
	 * @return TThe http result that was found (i.e. an http:// URL)
	 */
	public String getHttp() {
		return this.http;
	}

	/**
	 * Gets the index (starting at 0) where the http result was found
	 * @return Gets the index (starting at 0) where the http result was found
	 */
	public int getStart() {
		return this.start;
	}
}
