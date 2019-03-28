package io.spring.nohttp.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.resources.TextResource;

import java.io.File;
import java.net.URL;

/**
 * @author Rob Winch
 */
public class NoHttpPlugin implements Plugin<Project> {
	private Project project;

	@Override
	public void apply(Project project) {
		this.project = project;
		project.getPluginManager().apply("checkstyle");
		Configuration checkstyleConfiguration = project.getConfigurations().getByName("checkstyle");

		Configuration noHttpConfiguration = project.getConfigurations().create("nohttp");
		checkstyleConfiguration.extendsFrom(noHttpConfiguration);

		configureDefaultDependenciesForProject(noHttpConfiguration);
		createCheckstyleTaskForProject(checkstyleConfiguration);
	}

	private void createCheckstyleTaskForProject(Configuration configuration) {
		Checkstyle checkstyleTask = this.project
				.getTasks().create("nohttpCheckstyle", Checkstyle.class);
		File defaultCheckstyleFile = this.project.file("config/checkstyle/nohttp/nohttp-checkstyle.xml");
		if (defaultCheckstyleFile.exists()) {
			this.project.getLogger().debug("Found default checkstyle configuration, so configuring checkstyleTask to use it");
			checkstyleTask.setConfigFile(defaultCheckstyleFile);
		}
		checkstyleTask.setSource(this.project.fileTree(this.project.getProjectDir(), new Action<ConfigurableFileTree>() {
			@Override
			public void execute(ConfigurableFileTree files) {
				files.exclude("**/build/**");
				files.exclude(".git/**");
				files.exclude(".gradle/**");
			}
		}));
		checkstyleTask.setClasspath(this.project.files());
		checkstyleTask.setClasspath(configuration);
		checkstyleTask.doFirst(new Action<Task>() {
			@Override
			public void execute(Task task) {
				Logger logger = project.getLogger();
				if (checkstyleTask.getConfig() != null) {
					logger.debug("checkstyle config is not null");
					return;
				}
				logger.debug("checkstyle config is null, so defaulting it");
				// FIXME: Default from configuration vs classpath
				URL resource = getClass().getResource(
						"/io/spring/nohttp/checkstyle/default-nohttp-checkstyle.xml");
				TextResource defaultResource = NoHttpPlugin.this.project.getResources().getText().fromUri(resource);
				checkstyleTask.setConfig(defaultResource);
			}
		});
	}

	private void configureDefaultDependenciesForProject(Configuration configuration) {
		configuration.defaultDependencies(new Action<DependencySet>() {
			@Override
			public void execute(DependencySet dependencies) {
				dependencies.add(NoHttpPlugin.this.project.getDependencies().create("io.spring.nohttp:nohttp-checkstyle:0.0.1.BUILD-SNAPSHOT"));
			}
		});
	}
}
