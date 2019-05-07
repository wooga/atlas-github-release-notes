package wooga.gradle.githubReleaseNotes.tasks

import wooga.gradle.githubReleaseNotes.GithubIntegrationWithDefaultAuth
import wooga.gradle.githubReleaseNotes.GithubReleaseNotesPlugin
import wooga.gradle.githubReleaseNotes.RepoLayoutPresets

class GenerateReleaseNotesSpec extends GithubIntegrationWithDefaultAuth {

    def setupSpec() {
        RepoLayoutPresets.parallelDevelopmentOnTwoBranchesWithPullRequests(testRepo)
    }

    def setup() {
        buildFile << """
        ${applyPlugin(GithubReleaseNotesPlugin)}

        version = "1.0.0"
        """.stripIndent()
    }

    def "generates an output file"() {
        given: "a task"
        buildFile << """
        task generateTask (type:${GenerateReleaseNotes.name}){
            output = file("build/outputs/releaseNotes.txt")
            from = "v0.1.1"
        }
        """.stripIndent()

        and: "a future output"
        def outputFile = new File(projectDir, "build/outputs/releaseNotes.txt")

        assert !outputFile.exists()

        when:
        def result = runTasksSuccessfully("generateTask")

        then:
        result.wasExecuted("generateTask")
        outputFile.exists()
    }

    def "can set custom render closure"() {
        given: "a task"
        buildFile << """
        task generateTask (type:${GenerateReleaseNotes.name}){
            output = file("build/outputs/releaseNotes.txt")
            from = "v0.1.1"
        }
        """.stripIndent()


        and: "a custom render closure"
        buildFile << """
        generateTask.renderer = {changeset ->
            "my custom notes"
        }
        """.stripIndent()

        and: "a future output"
        def outputFile = new File(projectDir, "build/outputs/releaseNotes.txt")

        assert !outputFile.exists()

        when:
        def result = runTasksSuccessfully("generateTask")

        then:
        result.wasExecuted("generateTask")
        outputFile.exists()
        outputFile.text == "my custom notes"
    }
}
