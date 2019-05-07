package wooga.gradle.githubReleaseNotes


import org.kohsuke.github.GHPullRequest
import spock.lang.Ignore
import wooga.gradle.github.publish.GithubPublishPlugin

@Ignore
class GithubPublishIntegrationSpec extends GithubIntegrationWithDefaultAuth {

    def setup() {
        def squashPR = testRepo.setupPullRequestWithFileChange("A Test Change PR 2", "", "testBranch1", "testFile", "foo")
        squashPR.merge(null, squashPR.head.sha, GHPullRequest.MergeMethod.SQUASH)
        testRepo.getRef("heads/${squashPR.head.ref}").delete()

        def mergePR = testRepo.setupPullRequestWithFileChange("A Test Change PR 2", "", "testBranch2", "testFile2", "foo")
        mergePR.merge(null)
        testRepo.getRef("heads/${mergePR.head.ref}").delete()

        testRepo.createRelease("0.1.0", "v0.1.0")
    }

    def "some test"() {

        buildFile << """
        ${applyPlugin(GithubReleaseNotesPlugin)}
        ${applyPlugin(GithubPublishPlugin)}
                       
        version = "1.0.0"

        githubPublish {                   
            tagName = "foo"
            dependsOn project.tasks.getByName("generateReleaseNotes")

            body = {
                project.tasks.generateReleaseNotes.outputs.files.singleFile.text
            }
        }

        """.stripIndent()

        when:
        def result = runTasksSuccessfully("githubPublish")

        then:
        result.wasExecuted("generateReleaseNotes")
        def release = getTestRepo().getReleaseByTagName("foo")
        release.body == "klsjflksjfdk"

    }

    def "Detect"() {
        setup:
        testRepo.commit('commit 1')
        testRepo.commit('commit 2')
        testRepo.createRelease("1.0.0","v1.0.0")
        testRepo.commit('commit 3')
        testRepo.commit('commit 4')
        testRepo.commit('commit 5')
        testRepo.commit('commit 6')
        testRepo.createRelease("1.0.1","v1.0.1")
        testRepo.commit('commit 7')
        testRepo.commit('commit 8')
        def squashPR = testRepo.setupPullRequestWithFileChange("A Test Change PR 3", "", "testBranch3", "testFile3", "foo")
        squashPR.merge(null, squashPR.head.sha, GHPullRequest.MergeMethod.SQUASH)
        testRepo.createRelease("1.0.2","v1.0.2")
        testRepo.commit('commit 9')
        testRepo.commit('commit 10')
        testRepo.commit('commit 11')
        testRepo.createRelease("1.1.0","v1.1.0")
        testRepo.commit('commit 12')
        testRepo.commit('commit 13')
        testRepo.commit('commit 14')

        buildFile << """
        ${applyPlugin(GithubReleaseNotesPlugin)}
        ${applyPlugin(GithubPublishPlugin)}
                       
        version = "1.0.0"

        githubPublish {                   
            tagName = "foo"
            dependsOn project.tasks.getByName("generateReleaseNotes")

            body = {
                project.tasks.generateReleaseNotes.outputs.files.singleFile.text
            }
        }

        generateReleaseNotes {
            from = "v1.0.0"
            to = "v1.1.0"
        }             

        """.stripIndent()

        when:
        def result = runTasksSuccessfully("githubPublish")

        then:
        result.wasExecuted("generateReleaseNotes")
        def release = getTestRepo().getReleaseByTagName("foo")
        release.body == "klsjflksjfdk"

    }
}
