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

package io.spring.nohttp.gradle;

import org.gradle.api.file.ConfigurableFileTree;

import java.io.File;

/**
 * @author Rob Winch
 */
public class NoHttpExtension {

	private String toolVersion;

	private ConfigurableFileTree source;

	private File whitelistFile;

	public File getWhitelistFile() {
		return this.whitelistFile;
	}

	public void setWhitelistFile(File whitelistFile) {
		this.whitelistFile = whitelistFile;
	}

	public ConfigurableFileTree getSource() {
		return this.source;
	}

	public void setSource(ConfigurableFileTree source) {
		this.source = source;
	}

	public String getToolVersion() {
		return this.toolVersion;
	}

	public void setToolVersion(String toolVersion) {
		this.toolVersion = toolVersion;
	}
}
