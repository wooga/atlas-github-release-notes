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
    @Input
    final Property<String> releaseName

    @Input
    @Optional
    final Property<String> from

    @Input
    @Optional
    final Property<String> to

    @Input
    final Property<String> branch

    @Input
    final Property<GeneratorStrategy> strategy

    @Optional
    @Internal
    final Property<Closure<String>> renderer

    void setRenderer(Closure<String> renderer) {
        this.renderer.set(renderer)
    }

    @OutputFile
    final RegularFileProperty output

    GenerateReleaseNotes() {
        super(GenerateReleaseNotes.class)
        output = this.project.layout.fileProperty()
        from = this.project.objects.property(String)
        to = this.project.objects.property(String)
        releaseName = this.project.objects.property(String)
        branch = this.project.objects.property(String)
        branch.set(this.project.provider({this.repository.defaultBranch}))
        strategy = this.project.objects.property(GeneratorStrategy)
        renderer = this.project.objects.property(Closure) as Property<Closure>
    }

    @TaskAction
    void generate() {
        def detector = new DefaultChangeDetector(client, repository)
        def outputFile = output.get().asFile
        outputFile.parentFile.mkdirs()
        def s = strategy.get()
        def rawChanges = detector.detectChangesFromTag(
                from.getOrNull() as String,
                to.getOrNull() as String,
                branch.get())

        rawChanges.name = releaseName.get()

        if (renderer.present) {
            def renderer = this.renderer.get()
            def changeSet = s.mapChangeSet(rawChanges)
            outputFile.text = renderer.call(changeSet)
        }
        else {
            outputFile.text = s.render(rawChanges)
        }
    }
}
