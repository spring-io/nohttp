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

import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.Collection;

/**
 * @author Rob Winch
 */
public class NoHttpExtension {

	private String toolVersion;

	private FileTree source;

	public File getWhitelistsFile() {
		return this.whitelistsFile;
	}

	public void setWhitelistsFile(File whitelistsFile) {
		this.whitelistsFile = whitelistsFile;
	}

	private File whitelistsFile;

	public FileTree getSource() {
		return this.source;
	}

	public void setSource(FileTree source) {
		this.source = source;
	}

	public String getToolVersion() {
		return this.toolVersion;
	}

	public void setToolVersion(String toolVersion) {
		this.toolVersion = toolVersion;
	}
}
