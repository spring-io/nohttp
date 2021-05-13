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
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters
import java.io.File

/**
 * @author Rob Winch
 */
@RunWith(Parameterized::class)
class NoHttpCheckstylePluginITest {
    @Rule
    @JvmField
    val tempBuild = TemporaryFolder()

    @Rule
    @JvmField
    val tempBuild2 = TemporaryFolder()

    @Rule
    @JvmField
    val sharedTestKitDir = TemporaryFolder()

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun gradleVersions() = listOf("6.0.1", "6.8.3", "7.0", "7.0.1").map(GradleVersion::version)
    }

    @Parameter
    lateinit var gradleVersion: GradleVersion

    @Test
    fun httpsIsSuccess() {
        buildFile()

        tempBuild.newFile("has-https.txt")
                .writeText("""https://example.com""")

        val result = runner().build()
        assertThat(checkstyleNohttpTaskOutcome(result)).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun httpIsFailed() {
        buildFile()

        tempBuild.newFile("has-http.txt")
                .writeText("""http://example.com""")

        val result = runner().buildAndFail()
        assertThat(result.output).contains("Checkstyle files with violations: 1")
        assertThat(checkstyleNohttpTaskOutcome(result)).isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun upToDate() {
        buildFile()

        tempBuild.newFile("has-https.txt")
                .writeText("""https://example.com""")
        runner().build()
        
        val upToDateResult = runner().build()
        assertThat(checkstyleNohttpTaskOutcome(upToDateResult)).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun fromCache() {
        buildFile()

        tempBuild.newFile("has-https.txt")
                .writeText("""https://example.com""")
        runner(testKitDir = sharedTestKitDir.root).build()
        File(tempBuild.root, "build").deleteRecursively()
        
        val fromCacheResult = runner(testKitDir = sharedTestKitDir.root).build()
        assertThat(checkstyleNohttpTaskOutcome(fromCacheResult)).isEqualTo(TaskOutcome.FROM_CACHE)
    }

    @Test
    fun fromCacheWhenSwitchDirectories() {
        buildFile()
        
        tempBuild.newFile("has-https.txt")
                .writeText("""https://example.com""")
        tempBuild.root.copyRecursively(tempBuild2.root)
        runner(testKitDir = sharedTestKitDir.root).build()
        
        val fromCacheResult = runner(projectDir = tempBuild2.root, testKitDir = sharedTestKitDir.root).build()
        assertThat(checkstyleNohttpTaskOutcome(fromCacheResult)).isEqualTo(TaskOutcome.FROM_CACHE)
    }

    @Test
    fun usesDefaultConfigDirectory() {
        val configDirectory = tempBuild.newFolder("config", "nohttp")
        tempBuild.newFile("test.txt")
        buildFile("""
            checkstyleNohttp {
                doFirst {
                    println "config_loc = " + configProperties['config_loc']
                    println "configDirectory = " + configDirectory.asFile.getOrElse(null)?.canonicalPath
                }
            }
        """.trimIndent())

        val result = runner().build()
        assertThat(checkstyleNohttpTaskOutcome(result)).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output.lines())
                .contains("config_loc = null")
                .contains("configDirectory = ${configDirectory.canonicalPath}")
    }

    @Test
    fun worksIfCheckstyleConfigIsPresentInDefaultLocation() {
        val configDirectory = tempBuild.newFolder("config", "checkstyle")
        File(configDirectory, "checkstyle.xml").writeText("""
            <?xml version="1.0"?>
            <!DOCTYPE module PUBLIC
                    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                    "https://checkstyle.org/dtds/configuration_1_3.dtd">
            <module name="Checker">
                <module name="NewlineAtEndOfFile">
                    <property name="lineSeparator" value="lf"/>
                </module>
            </module>
        """.trimIndent())
        buildFile()

        val result = runner().build()
        assertThat(checkstyleNohttpTaskOutcome(result)).isEqualTo(TaskOutcome.SUCCESS)
    }

    fun checkstyleNohttpTaskOutcome(build: BuildResult): TaskOutcome? {
        return build.task(":" + NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME)?.outcome
    }

    fun runner(projectDir: File = tempBuild.root, testKitDir: File? = null): GradleRunner {
        var gradleRunner = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .forwardOutput()
                .withGradleVersion(gradleVersion.version)
        val args = mutableListOf(NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME, "--stacktrace")
        if (testKitDir != null) {
            args.add("--build-cache")
            gradleRunner = gradleRunner.withTestKitDir(testKitDir)
        }
        args.addAll(listOf("--warning-mode", "fail"))
        return gradleRunner.withArguments(args)
    }

    fun buildFile(content: String = "") {
        val build = tempBuild.newFile("build.gradle");
        val workingDir = File(System.getProperty("user.dir"))
        val rootDir = workingDir.parentFile
        val nohttpDir = File(rootDir, "nohttp")
        val nohttpCheckstyleDir = File(rootDir, "nohttp-checkstyle")
        build.writeText("""
            plugins {
                id 'io.spring.nohttp'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                nohttp fileTree(dir: '${nohttpDir.absolutePath}', include: '**/build/libs/*.jar')
                nohttp fileTree(dir: '${nohttpCheckstyleDir.absolutePath}', include: '**/build/libs/*.jar')
            }

            $content
        """.trimIndent())
    }
}
