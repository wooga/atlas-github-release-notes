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
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.slf4j.Logger
import wooga.gradle.github.base.GithubBasePlugin
import wooga.gradle.githubReleaseNotes.tasks.GenerateReleaseNotes

class GithubReleaseNotesPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = "GithubReleaseNotes"
    static final String GENERATE_RELEASE_NOTES_TASK = "generateReleaseNotes"

    static Logger logger = Logging.getLogger(GithubReleaseNotesPlugin)

    @Override
    void apply(Project project) {
        project.pluginManager.apply(GithubBasePlugin)
        createDefaultTasks(project)

        project.tasks.withType(GenerateReleaseNotes, new Action<GenerateReleaseNotes>() {
            @Override
            void execute(GenerateReleaseNotes task) {
                def defaultStrategy = new DefaultGeneratorStrategy()
                task.strategy.set(defaultStrategy)
                task.releaseName.set(project.provider({project.version.toString()}))
                task.output.set(new File(project.buildDir,"outputs/release-notes.txt"))
                //task.output.set(project.layout.buildDirectory.dir("outputs").map({ it.file("release-notes.txt") }))
            }
        })
    }

    protected static createDefaultTasks(Project project) {
        project.tasks.create(GENERATE_RELEASE_NOTES_TASK, GenerateReleaseNotes)
    }
}
