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

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.ExternalResourceHolder;
import com.puppycrawl.tools.checkstyle.api.FileText;
import io.spring.nohttp.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * NoHttpCheck verifies that your build has no http:// URLs but ignores URLs that cannot
 * be https://.
 * </p>
 *
 * <p>
 * NOTE: If you are using Gradle, see nohttp-gradle project.
 * </p>
 *
 * <p>
 * While many checkstyle configurations only impact your source and resources, it is
 * important that no http checks are performed on all the files within your repository.
 * For example, you will want to ensure that you validate your build files like pom.xml
 * and build.gradle.
 * </p>
 *
 * <h2>Configurations</h2>
 * <p>
 * An example configuration can be found below:
 * </p>
 *
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN"
 *         "https://www.puppycrawl.com/dtds/configuration_1_3.dtd"&gt;
 * &lt;module name="Checker"&gt;
 *
 *     &lt;!-- Configure checker to use UTF-8 encoding --&gt;
 *     &lt;property name="charset" value="UTF-8"/&gt;
 *     &lt;!-- Configure checker to run on files with all extensions --&gt;
 *     &lt;property name="fileExtensions" value=""/&gt;
 *
 *     &lt;module name="io.spring.nohttp.checkstyle.check.NoHttpCheck"&gt;
 *     &lt;/module&gt;
 * &lt;/module&gt;
 * </pre>
 *
 * <p>
 * If you find the need to exclude additional URL patterns, you can do so by including.
 * </p>
 *
 * <pre>
 * &lt;module name="io.spring.nohttp.checkstyle.check.NoHttpCheck"&gt;
 *     &lt;!-- the file name to load for white listing. If an empty String nothing is used --&gt;
 *     &lt;property name="whitelistFileName" value="etc/nohttp/whitelist.lines"/&gt;
 * &lt;/module&gt;
 * </pre>
 *
 * <p>
 * It is important to note that you use checkstyle properties to load the file as well. If you want to make the property optional, you can specify a default of empty String in which case the additional whitelist is ignored.
 * </p>
 *
 * <pre>
 * &lt;module name="io.spring.nohttp.checkstyle.check.NoHttpCheck"&gt;
 *     &lt;!-- the file name to load for white listing. If an empty String nothing is used --&gt;
 *     &lt;property name="whitelistFileName" value="${nohttp.checkstyle.whitelistFileName}" default=""/&gt;
 * &lt;/module&gt;
 * </pre>
 *
 * @author Rob Winch
 */
public class NoHttpCheck extends AbstractFileSetCheck implements ExternalResourceHolder {
	private HttpMatcher matcher;

	private String whitelistFileName = "";

	public void setWhitelistFileName(String whitelistFileName) {
		if (whitelistFileName == null) {
			throw new IllegalArgumentException("whitelistFileName cannot be null");
		}
		this.whitelistFileName = whitelistFileName;
	}

	private boolean isWhitelistFileSet() {
		return !this.whitelistFileName.isEmpty();
	}

	private InputStream createWhitelistInputStream() {
		File whitelistFile = new File(this.whitelistFileName);
		try {
			return new FileInputStream(whitelistFile);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Could not load file '" + whitelistFile +"'", e);
		}
	}

	@Override
	protected void finishLocalSetup() throws CheckstyleException {
		RegexHttpMatcher matcher = new RegexHttpMatcher(RegexPredicate.createDefaultUrlWhitelist());

		if (this.isWhitelistFileSet()) {
			InputStream inputStream = createWhitelistInputStream();
			matcher.addHttpWhitelist(RegexPredicate.createWhitelistFromPatterns(inputStream));
		}
		this.matcher = matcher;
	}

	@Override
	protected void processFiltered(File file, FileText fileText)
			throws CheckstyleException {
		int lineNum = 0;
		for (int index = 0; index < fileText.size(); index++) {
			final String line = fileText.get(index);
			lineNum++;
			List<HttpMatchResult> results = this.matcher.findHttp(line);
			for(HttpMatchResult result : results) {
				log(lineNum, result.getStart() + 1, "nohttp", result.getHttp());
			}
		}
	}

	@Override
	public Set<String> getExternalResourceLocations() {
		Set<String> result = new HashSet<>();
		if (isWhitelistFileSet()) {
			result.add(this.whitelistFileName);
		}
		return result;
	}
}
