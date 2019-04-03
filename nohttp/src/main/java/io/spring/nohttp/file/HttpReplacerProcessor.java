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

import java.io.File;
import java.util.List;

/**
 * @author Rob Winch
 */
public class HttpReplacerProcessor extends HttpProcessor {
	private final HttpReplacer replacer;

	public HttpReplacerProcessor(HttpReplacer replacer) {
		if (replacer == null) {
			throw new IllegalArgumentException("replacer cannot be null");
		}
		this.replacer = replacer;
	}

	@Override
	protected List<HttpMatchResult> processHttpInFile(File file) {
		String originalText = FileUtils.readTextFrom(file);

		HttpReplaceResult result = this.replacer.replaceHttp(originalText);
		if (result.isReplacement()) {
			FileUtils.writeTextTo(result.getResult(), file);
		}

		return result.getMatches();
	}
}
