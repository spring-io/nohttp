package io.spring.nohttp.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.plugins.quality.Checkstyle;

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

	private void createCheckstyleTaskForProject(Configuration checkstyleConfiguration) {
		Checkstyle checkstyleTask = this.project
				.getTasks().create("nohttpCheckstyle", Checkstyle.class);
		checkstyleTask.setConfigFile(this.project.file("nohttp-checkstyle.xml"));
		checkstyleTask.setSource(this.project.fileTree(this.project.getProjectDir(), new Action<ConfigurableFileTree>() {
			@Override
			public void execute(ConfigurableFileTree files) {
				files.exclude("**/build/**");
				files.exclude(".git/**");
				files.exclude(".gradle/**");
			}
		}));
		checkstyleTask.setClasspath(this.project.files());
		checkstyleTask.setClasspath(checkstyleConfiguration);
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
