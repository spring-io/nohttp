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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * @author Rob Winch
 */
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


    fun checkstyleNohttpTaskOutcome(build: BuildResult): TaskOutcome? {
        return build.task(":" + NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME)?.outcome
    }

    fun runner(projectDir: File = tempBuild.root, testKitDir: File? = null): GradleRunner {
        var gradleRunner = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
        val args = mutableListOf(NoHttpCheckstylePlugin.CHECKSTYLE_NOHTTP_TASK_NAME, "--stacktrace")
        if (testKitDir != null) {
            args.add("--build-cache")
            gradleRunner = gradleRunner.withTestKitDir(testKitDir)
        }
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
