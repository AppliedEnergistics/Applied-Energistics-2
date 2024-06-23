package appengbuild;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@DisableCachingByDefault(because = "Just prints a version")
public abstract class PrintProjectVersion extends DefaultTask {
    @Inject
    public PrintProjectVersion(Project project) {
        getVersion().set(project.provider(() -> project.getVersion().toString()));
        getOutputs().upToDateWhen(task -> false);
    }

    @Internal
    public abstract Property<String> getVersion();

    @TaskAction
    public void printVersion() throws IOException {
        var version = getVersion().get();

        var githubOutput = System.getenv("GITHUB_OUTPUT");
        if (githubOutput != null) {
            Files.writeString(Paths.get(githubOutput), "version=" + version + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }

        var githubEnv = System.getenv("GITHUB_ENV");
        if (githubEnv != null) {
            Files.writeString(Paths.get(githubEnv), "VERSION=" + version + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }

        System.out.println("Project version: " + version);
    }
}
