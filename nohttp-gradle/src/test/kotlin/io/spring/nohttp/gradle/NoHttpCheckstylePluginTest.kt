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

package io.spring.nohttp.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * @author Rob Winch
 */
class NoHttpCheckstylePluginTest {
    @Rule
    @JvmField
    val tempBuild = TemporaryFolder()

    @Test
    fun appliesCheckstylePlugin() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)

        assertThat(project.plugins.hasPlugin(CheckstylePlugin::class.java));
    }

    // See CheckstylePluginTest

    @Test
    fun configuredNohttpExtensionWhenWhitelistLinesFoundThenExists() {
        val project = projectWithTempDirs().build()
        val whitelistFile = project.file(NoHttpCheckstylePlugin.DEFAULT_WHITELIST_FILE_PATH)
        whitelistFile.touch()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.whitelistsFile).isEqualTo(whitelistFile)
    }

    @Test
    fun configuredNohttpExtensionWhenBuildThenSourceIncludesBuild() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        val build = project.file("build.gradle").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).containsOnly(build)
    }

    @Test
    fun configuredNohttpExtensionWhenGitThenExcludes() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        project.file(".git/foo").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).isEmpty()
    }

    @Test
    fun configuredNohttpExtensionWhenSpringHandlersThenExcludes() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        project.file("src/main/resources/META-INF/spring.handlers").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).isEmpty()
    }

    @Test
    fun configuredNohttpExtensionWhenSpringSchemasThenExcludes() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        project.file("src/main/resources/META-INF/spring.schemas").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).isEmpty()
    }

    @Test
    fun configuredNohttpExtensionWhenDotGradleThenExcludes() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        project.file(".gradle/foo").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).isEmpty()
    }

    @Test
    fun configuredNohttpExtensionWhenDotIdeaThenExcludes() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        project.file(".idea/foo").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).isEmpty()
    }

    @Test
    fun configuredNohttpExtensionWhenDotClassThenExcludes() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        project.file("Main.class").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).isEmpty()
    }

    @Test
    fun configuredNohttpExtensionWhenJksThenExcludes() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        project.file("something.jks").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).isEmpty()
    }

    @Test
    fun configuredNohttpExtensionWhenBuildDirThenExcludes() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        File(project.buildDir, "something").touch()

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension

        assertThat(nohttp.source.files).isEmpty()
    }

    @Test
    fun configuredCheckstyleNohttpTask() {
        val project = projectWithTempDirs()
                .build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)

        val task: Checkstyle = project.tasks.findByName(NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME)!! as Checkstyle

        assertThat(task.description).isEqualTo("Checks for illegal uses of http://")
        assertThat(task.classpath).isEqualTo(project.configurations.findByName(NoHttpCheckstylePlugin.CHECKSTYLE_CONFIGURATION_NAME))
        val DOLLAR = "$"
        assertThat(task.configFile.readText()).contains("""<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN"
        "https://www.puppycrawl.com/dtds/configuration_1_3.dtd">
<module name="Checker">

    <!-- Configure checker to use UTF-8 encoding -->
    <property name="charset" value="UTF-8"/>
    <!-- Configure checker to run on files with these extensions -->
    <property name="fileExtensions" value=""/>

    <module name="io.spring.nohttp.checkstyle.check.NoHttpCheck">
        <property name="whitelistFileName" value="${DOLLAR}{nohttp.checkstyle.whitelistFileName}" default=""/>
    </module>
</module>""")
        assertThat(task.configProperties).isEmpty()
        assertThat(task.reports.xml.destination).isEqualTo(project.file("build/reports/checkstyle/nohttp.xml"))
        assertThat(task.reports.html.destination).isEqualTo(project.file("build/reports/checkstyle/nohttp.html"))
        assertThat(task.isIgnoreFailures).isFalse()
        assertThat(task.isShowViolations).isTrue()
        assertThat(task.maxErrors).isEqualTo(0)
        assertThat(task.maxWarnings).isEqualTo(Integer.MAX_VALUE)
    }

    @Test
    fun configuredCheckstyleNohttpTaskWhenDefaultWhitelist() {
        val project = projectWithTempDirs()
                .build()
        val whitelistFile = project.file(NoHttpCheckstylePlugin.DEFAULT_WHITELIST_FILE_PATH).touch()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)

        val task: Checkstyle = project.tasks.findByName(NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME)!! as Checkstyle

        assertThat(task.configProperties).containsEntry("nohttp.checkstyle.whitelistFileName", whitelistFile)
    }

    @Test
    fun configuredCheckstyleNohttpTaskWhenCustomWhitelist() {
        val project = projectWithTempDirs()
                .build()
        val whitelistFile = project.file("customFile").touch()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)

        val nohttp: NoHttpExtension = project.extensions.getByName(NoHttpCheckstylePlugin.NOHTTP_EXTENSION_NAME) as NoHttpExtension
        nohttp.whitelistsFile = whitelistFile
        val task: Checkstyle = project.tasks.findByName(NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME)!! as Checkstyle

        assertThat(task.configProperties).containsEntry("nohttp.checkstyle.whitelistFileName", whitelistFile)
    }

    @Test
    fun addsTaskToCheckLifecycleTask() {
        val project = projectWithTempDirs().build()
        project.pluginManager.apply(NoHttpCheckstylePlugin::class.java)
        project.pluginManager.apply(JavaBasePlugin::class.java)

        val check = project.tasks.findByName("check")!!
        assertThat(check.taskDependencies.getDependencies(check).map { t -> t.name }).contains(NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME)
    }

    fun projectWithTempDirs(): ProjectBuilder {
        return ProjectBuilder.builder()
                .withProjectDir(tempBuild.newFolder())
                .withGradleUserHomeDir(tempBuild.newFolder())
    }

    fun File.touch(): File {
        val f = this
        if (f.exists()) {
            return this
        }
        f.parentFile.mkdirs()
        f.writeText("")
        return f
    }
}
