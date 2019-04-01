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

import java.util.List;

/**
 * Finds http:// URLs within text
 *
 * @author Rob Winch
 */
public interface HttpMatcher {
	/**
	 * Finds all http:// URLs within text
	 * @param text The text to find http:// URLs within
	 * @return the {@link HttpMatchResult}s that point to where the matches were found.
	 */
	List<HttpMatchResult> findHttp(String text);
}
