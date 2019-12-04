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

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.resources.TextResource;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * @author Rob Winch
 */
public class NoHttpCheckstylePlugin implements Plugin<Project> {
	private static final String NOHTTP_VERSION = determineNohttpVersion();

	public static final String DEFAULT_WHITELIST_FILE_PATH = "config/nohttp/whitelist.lines";

	public static final String LEGACY_WHITELIST_FILE_PATH = "etc/nohttp/whitelist.lines";

	public static final String NOHTTP_EXTENSION_NAME = "nohttp";

	public static final String CHECKSTYLE_CONFIGURATION_NAME = "checkstyle";

	public static final String CHECKSTYLE_NOHTTP_TASK_NAME = "checkstyleNohttp";

	public static final String DEFAULT_CONFIGURATION_NAME = "nohttp";

	private static final String CHECK_TASK_NAME = LifecycleBasePlugin.CHECK_TASK_NAME;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Project project;

	private NoHttpExtension extension;

	@Override
	public void apply(Project project) {
		this.project = project;
		this.extension = this.project.getExtensions().create(NOHTTP_EXTENSION_NAME, NoHttpExtension.class);
		this.extension.setToolVersion(NOHTTP_VERSION);
		this.extension.setSource(project.fileTree(project.getProjectDir(), new Action<ConfigurableFileTree>() {
			@Override
			public void execute(ConfigurableFileTree files) {
				String projectDir = project.getProjectDir().getAbsolutePath();
				files.exclude(createBuildExclusion(projectDir, project));
				project.subprojects(new Action<Project>() {
					@Override
					public void execute(Project p) {
						files.exclude(createBuildExclusion(projectDir, p));
					}
				});
				files.exclude(".git/**");
				files.exclude(".gradle/**");
				files.exclude("buildSrc/.gradle/**");
				files.exclude(".idea/**");
				files.exclude("**/*.class");
				files.exclude("**/*.hprof");
				files.exclude("**/*.jar");
				files.exclude("**/*.jpg");
				files.exclude("**/*.jks");
				files.exclude("**/spring.handlers");
				files.exclude("**/spring.schemas");
				files.exclude("**/spring.tooling");
			}
		}));
		File legacyWhiteListFile = project.file(LEGACY_WHITELIST_FILE_PATH);
		if (legacyWhiteListFile.exists()) {
			this.extension.setWhitelistFile(legacyWhiteListFile);
		}
		File defaultWhiteListFile = project.file(DEFAULT_WHITELIST_FILE_PATH);
		if (defaultWhiteListFile.exists()) {
			this.extension.setWhitelistFile(defaultWhiteListFile);
		}

		project.getPluginManager().apply(CheckstylePlugin.class);
		Configuration checkstyleConfiguration = project.getConfigurations().getByName(CHECKSTYLE_CONFIGURATION_NAME);

		Configuration noHttpConfiguration = project.getConfigurations().create(DEFAULT_CONFIGURATION_NAME);
		checkstyleConfiguration.extendsFrom(noHttpConfiguration);

		configureDefaultDependenciesForProject(noHttpConfiguration);
		createCheckstyleTaskForProject(checkstyleConfiguration);
		configureCheckTask();
	}

	private String createBuildExclusion(String projectDir, Project p) {
		File buildDir = p.getBuildDir();
		String path = buildDir.getAbsolutePath().replace(projectDir + File.separator, "");
		String pattern = path + "/**";
		return pattern;
	}

	private void createCheckstyleTaskForProject(Configuration configuration) {
		Logger logger = this.logger;
		Project project = this.project;
		NoHttpExtension extension = this.extension;
		Checkstyle checkstyleTask = project
				.getTasks().create("checkstyleNohttp", Checkstyle.class);
		checkstyleTask.setDescription("Checks for illegal uses of http://");

		ConventionMapping taskMapping = checkstyleTask.getConventionMapping();
		taskMapping.map("classpath", new Callable<FileCollection>() {
			@Override
			public FileCollection call() throws Exception {
				return configuration;
			}
		});
		taskMapping.map("source", new Callable<FileTree>() {
			@Override
			public FileTree call() throws Exception {
				return extension.getSource();
			}
		});
		taskMapping.map("configProperties", new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				Map<String, Object> configProperties = new HashMap<>();
				File whitelistFile = extension.getWhitelistFile();
				if (whitelistFile != null) {
					logger.debug("Using whitelist at {}", whitelistFile);
					configProperties.put("nohttp.checkstyle.whitelistFileName", project.relativePath(whitelistFile));
				}
				configProperties.put("config_loc", project.relativePath(getConfigLocation()));
				return configProperties;
			}
		});
		taskMapping.map("config", new Callable<TextResource>() {
			@Override
			public TextResource call() throws Exception {
				File configLoc = getConfigLocation();
				File defaultCheckstyleFile = new File(configLoc, "checkstyle.xml");
				if (defaultCheckstyleFile.exists()) {
					logger.debug("Found default checkstyle configuration, so configuring checkstyleTask to use it");
					return project.getResources().getText().fromFile(defaultCheckstyleFile);
				}
				logger.debug("No checkstyle configuration provided, so using the default.");
				URL resource = getClass().getResource(
						"/io/spring/nohttp/checkstyle/default-nohttp-checkstyle.xml");
				return project.getResources().getText().fromUri(resource);
			}
		});
	}

	private File getConfigLocation() {
		File whitelistFile = this.extension.getWhitelistFile();
		if (whitelistFile != null) {
			return whitelistFile.getParentFile();
		}
		File legacy = this.project.file("etc/nohttp");
		if (legacy.exists()) {
			return legacy;
		}
		return this.project.file("config/nohttp");
	}

	private void configureDefaultDependenciesForProject(Configuration configuration) {
		configuration.defaultDependencies(new Action<DependencySet>() {
			@Override
			public void execute(DependencySet dependencies) {
				NoHttpExtension extension = NoHttpCheckstylePlugin.this.extension;
				dependencies.add(NoHttpCheckstylePlugin.this.project.getDependencies().create("io.spring.nohttp:nohttp-checkstyle:" + extension.getToolVersion()));
			}
		});
	}

	private void configureCheckTask() {
		this.project.getTasks().withType(Task.class, new Action<Task>() {
			public void execute(Task task) {
				if (CHECK_TASK_NAME.equals(task.getName())) {
					task.dependsOn(new Callable() {
						@Override
						public Object call() {
							return CHECKSTYLE_NOHTTP_TASK_NAME;
						}
					});
				}
			}
		});
	}

	/**
	 * Gets the nohttp version from the Manifest
	 *
	 * Code used from
	 * <a href="https://github.com/spring-projects/spring-boot/blob/v2.1.4.RELEASE/spring-boot-project/spring-boot-tools/spring-boot-gradle-plugin/src/main/java/org/springframework/boot/gradle/plugin/SpringBootPlugin.java#L139-L165">SpringBootPlugin</a>
	 * @return the nohttp version
	 */
	private static String determineNohttpVersion() {
		Class<?> clazz = NoHttpCheckstylePlugin.class;
		String implementationVersion = clazz.getPackage()
				.getImplementationVersion();
		if (implementationVersion != null) {
			return implementationVersion;
		}
		URL codeSourceLocation = clazz
				.getProtectionDomain().getCodeSource().getLocation();
		try {
			URLConnection connection = codeSourceLocation.openConnection();
			if (connection instanceof JarURLConnection) {
				return getImplementationVersion(
						((JarURLConnection) connection).getJarFile());
			}
			try (JarFile jarFile = new JarFile(new File(codeSourceLocation.toURI()))) {
				return getImplementationVersion(jarFile);
			}
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static String getImplementationVersion(JarFile jarFile) throws IOException {
		return jarFile.getManifest().getMainAttributes()
				.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
	}
}
