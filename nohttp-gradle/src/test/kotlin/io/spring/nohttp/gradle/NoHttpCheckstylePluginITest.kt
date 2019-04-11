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
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * @author Rob Winch
 */
class NoHttpCheckstylePluginITest {
    @Rule
    @JvmField
    val tempBuild = TemporaryFolder()

    @Test
    fun httpsIsSuccess() {
        buildFile()

        tempBuild.newFile("has-https.txt")
                .writeText("""https://example.com""")

        val result = runner().build()
        assertThat(checkstyleNohttpTaskOutcome(result)).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    fun httpIsFailed() {
        buildFile()

        tempBuild.newFile("has-http.txt")
                .writeText("""http://example.com""")

        val result = runner().buildAndFail()
        assertThat(result.output).contains("Checkstyle files with violations: 1")
        assertThat(checkstyleNohttpTaskOutcome(result)).isEqualTo(TaskOutcome.FAILED);
    }

    fun checkstyleNohttpTaskOutcome(build: BuildResult): TaskOutcome? {
        return build.task(":" + NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME)?.outcome
    }

    fun runner(): GradleRunner {
        return GradleRunner.create()
                .withProjectDir(tempBuild.root)
                .withArguments(NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME, "--stacktrace")
                .withPluginClasspath()
    }

    // FIXME: the fileTree needs to be specified separately
    fun buildFile(content: String = "") {
        val build = tempBuild.newFile("build.gradle");
        build.writeText("""
            plugins {
                id 'io.spring.nohttp'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                nohttp fileTree(dir: '/home/rwinch/code/spring-io/nohttp/', include: '**/*.jar')
            }

            $content
        """.trimIndent())
    }
}