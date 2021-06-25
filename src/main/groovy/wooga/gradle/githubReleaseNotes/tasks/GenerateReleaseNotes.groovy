package wooga.gradle.githubReleaseNotes.tasks

import com.wooga.github.changelog.DefaultChangeDetector
import com.wooga.github.changelog.GeneratorStrategy
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import wooga.gradle.github.base.tasks.internal.AbstractGithubTask

class GenerateReleaseNotes extends AbstractGithubTask {

    private final Property<String> releaseName

    @Input
    Property<String> getReleaseName() {
        releaseName
    }

    private final Property<String> from

    @Input
    @Optional
    Property<String> getFrom() {
        from
    }

    private final Property<String> to

    @Input
    @Optional
    Property<String> getTo() {
        to
    }

    private final Property<String> branch

    @Input
    Property<String> getBranch() {
        branch
    }

    private final Property<GeneratorStrategy> strategy

    @Input
    Property<GeneratorStrategy> getStrategy() {
        strategy
    }

    private final Property<Closure<String>> renderer

    @Internal
    Property<Closure<String>> getRenderer() {
        renderer
    }

    void setRenderer(Closure<String> renderer) {
        this.renderer.set(renderer)
    }

    private final RegularFileProperty output

    @OutputFile
    RegularFileProperty getOutput() {
        output
    }

    GenerateReleaseNotes() {
        super(GenerateReleaseNotes.class)
        output = this.project.objects.fileProperty()
        from = this.project.objects.property(String)
        to = this.project.objects.property(String)
        releaseName = this.project.objects.property(String)
        branch = this.project.objects.property(String)
        branch.set(this.project.provider({ this.repository.defaultBranch }))
        strategy = this.project.objects.property(GeneratorStrategy)
        renderer = this.project.objects.property(Closure) as Property<Closure>
    }

    @TaskAction
    void generate() {
        def detector = new DefaultChangeDetector(client, repository)
        def outputFile = output.get().asFile
        outputFile.parentFile.mkdirs()
        def s = strategy.get()
        def rawChanges = detector.detectChanges(
                from.getOrNull() as String,
                to.getOrNull() as String,
                branch.get())

        rawChanges.name = releaseName.get()

        if (renderer.present) {
            def renderer = this.renderer.get()
            def changeSet = s.mapChangeSet(rawChanges)
            outputFile.text = renderer.call(changeSet)
        } else {
            outputFile.text = s.render(rawChanges)
        }
    }
}
