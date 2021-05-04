/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.githubReleaseNotes

import com.wooga.github.changelog.DefaultGeneratorStrategy
import org.gradle.api.Plugin
import org.gradle.api.Project
import wooga.gradle.github.base.GithubBasePlugin
import wooga.gradle.githubReleaseNotes.tasks.GenerateReleaseNotes

class GithubReleaseNotesPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = "GithubReleaseNotes"
    static final String GENERATE_RELEASE_NOTES_TASK = "generateReleaseNotes"

    @Override
    void apply(Project project) {
        project.pluginManager.apply(GithubBasePlugin)
        createDefaultTasks(project)

        project.tasks.withType(GenerateReleaseNotes) { GenerateReleaseNotes task ->
            def defaultStrategy = new DefaultGeneratorStrategy()
            task.strategy.convention(defaultStrategy)
            task.releaseName.convention(project.provider({project.version.toString()}))
            task.output.convention(project.layout.buildDirectory.file("outputs/release-notes.txt"))
        }
    }

    protected static createDefaultTasks(Project project) {
        project.tasks.register(GENERATE_RELEASE_NOTES_TASK, GenerateReleaseNotes)
    }
}
