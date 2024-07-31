package appengbuild;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

import java.util.Collections;

public class ProjectDefaultsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.setGroup("appeng");

        var java = project.getExtensions().getByType(JavaPluginExtension.class);
        java.toolchain(spec -> {
            spec.getLanguageVersion().set(project.getProviders().gradleProperty("java_version").map(JavaLanguageVersion::of));
        });

        // ensure everything uses UTF-8 and not some random codepage chosen by Gradle
        project.getTasks().withType(JavaCompile.class, ProjectDefaultsPlugin::setupCompiler);
        }

    private static void setupCompiler(JavaCompile task) {
        var options = task.getOptions();
        options.setEncoding("UTF-8");
        options.setDeprecation(false);
        Collections.addAll(
                options.getCompilerArgs(),
                "-Xmaxerrs", "9999"
        );
    }
}
