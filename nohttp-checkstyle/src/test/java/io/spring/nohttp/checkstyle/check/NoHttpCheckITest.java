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

import com.puppycrawl.tools.checkstyle.AstTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.ModuleFactory;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.RootModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 * @author Phillip Webb
 * @author Rob Winch
 */
@RunWith(Parameterized.class)
public class NoHttpCheckITest {


	private static final boolean RUNNING_ON_WINDOWS = System.getProperty("os.name")
			.toLowerCase().contains("win");

	private static final File SOURCES_DIR = new File("src/test/resources/source");

	private static final File CHECKS_DIR = new File("src/test/resources/check");

	private static final File CONFIGS_DIR = new File("src/test/resources/config");

	private static final File WHITELIST_DIR = new File("src/test/resources/whitelist");

	private final Parameter parameter;

	public NoHttpCheckITest(Parameter parameter) {
		this.parameter = parameter;
	}


	@Test
	public void processHasExpectedResults() throws Exception {
		Configuration configuration = loadConfiguration();
		RootModule rootModule = createRootModule(configuration);
		try {
			processAndCheckResults(rootModule);
		}
		finally {
			rootModule.destroy();
		}
	}

	private Configuration loadConfiguration() throws Exception {
		Properties properties = this.parameter.getProperties();
		try (InputStream inputStream = this.parameter.getConfig()) {
			Configuration configuration = ConfigurationLoader.loadConfiguration(
					new InputSource(inputStream),
					new PropertiesExpander(properties),
					ConfigurationLoader.IgnoredModulesOptions.EXECUTE,
					ThreadModeSettings.SINGLE_THREAD_MODE_INSTANCE);
			return configuration;
		}
	}

	private RootModule createRootModule(Configuration configuration)
			throws CheckstyleException {
		ModuleFactory factory = new PackageObjectFactory(
				Checker.class.getPackage().getName(), getClass().getClassLoader());
		RootModule rootModule = (RootModule) factory
				.createModule(configuration.getName());
		rootModule.setModuleClassLoader(getClass().getClassLoader());
		rootModule.configure(configuration);
		return rootModule;
	}

	private void processAndCheckResults(RootModule rootModule)
			throws CheckstyleException {
		rootModule.addListener(this.parameter.getAssersionsListener());
		rootModule.process(Arrays.asList(this.parameter.getSourceFile()));
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Parameter> files() throws IOException {
		return Arrays.stream(SOURCES_DIR.list((dir, name) -> !name.startsWith(".")))
				.map(Parameter::new).collect(Collectors.toList());
	}

	private static class Parameter {

		private final String name;

		private final File sourceFile;

		private final AssertionsAuditListener assersionsListener;

		private final InputStream config;

		private final Properties properties;

		Parameter(String sourceName) {
			int suffixIndex = sourceName.lastIndexOf(".");
			this.name = sourceName.substring(0, suffixIndex);
			this.sourceFile = new File(SOURCES_DIR, sourceName);
			File configFile = new File(CONFIGS_DIR, this.name + ".xml");
			if (configFile.exists()) {
				try {
					this.config = new FileInputStream(configFile);
				} catch (IOException e) {
					throw new RuntimeException("Failed to load " + configFile, e);
				}
			} else {
				this.config = NoHttpCheck.class.getClassLoader().getResourceAsStream("io/spring/nohttp/checkstyle/default-nohttp-checkstyle.xml");
			}
			Properties properties = new Properties();
			File whitelistFile = new File(WHITELIST_DIR, this.name + ".lines");
			if (whitelistFile.exists()) {
				properties.put("nohttp.checkstyle.whitelistFileName", whitelistFile.getAbsolutePath());
			}
			this.properties = properties;
			this.assersionsListener = new AssertionsAuditListener(
					readChecks(this.name + ".txt"));
		}

		private List<String> readChecks(String name) {
			try {
				return Files.readAllLines(new File(CHECKS_DIR, name).toPath());
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		public Properties getProperties() {
			return this.properties;
		}

		public File getSourceFile() {
			return this.sourceFile;
		}

		public InputStream getConfig() {
			return this.config;
		}

		public AssertionsAuditListener getAssersionsListener() {
			return this.assersionsListener;
		}

		@Override
		public String toString() {
			return this.name;
		}

	}
}
