package io.spring.nohttp.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.tasks.JavaExec;

import static io.spring.nohttp.gradle.NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME;

/**
 * @author Rob Winch
 */
public class NoHttpCliPlugin implements Plugin<Project> {
	private Project project;

	private NoHttpExtension extension;

	@Override
	public void apply(Project project) {
		this.project = project;
		this.extension = (NoHttpExtension) this.project.getExtensions().getByName(NOHTTP_EXTENSION_NAME);

		Configuration nohttpCli = this.project.getConfigurations().create("nohttp-cli");
		configureDefaultDependenciesForProject(nohttpCli);

		JavaExec nohttp = project.getTasks().create("nohttp", JavaExec.class);
		nohttp.setDescription("Runs nohttp");

		nohttp.setMain("org.springframework.boot.loader.JarLauncher");
		nohttp.setClasspath(nohttpCli);
	}


	private void configureDefaultDependenciesForProject(Configuration configuration) {
		configuration.defaultDependencies(new Action<DependencySet>() {
			@Override
			public void execute(DependencySet dependencies) {
				NoHttpExtension extension = NoHttpCliPlugin.this.extension;
				dependencies.add(NoHttpCliPlugin.this.project.getDependencies().create("io.spring.nohttp:nohttp-cli:" + extension.getToolVersion()));
			}
		});
	}
}
