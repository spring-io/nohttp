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

import java.io.File;
import java.util.function.Predicate;

/**
 *
 * @author Rob Winch
 */
public abstract class FilePredicates {

	/**
	 * Predicate that determines if a file has the specified name
	 * @param fileName the name to check
	 * @return a Predicate that determines if a file has the specified name
	 */
	public static Predicate<File> fileHasName(String fileName) {
		if (fileName == null) {
			throw new IllegalArgumentException("fileName cannot be null");
		}
		return file -> file.getName().equals(fileName);
	}

	private FilePredicates() {}
}
