package appengbuild;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Sets up a {@code jmh} source set (src/jmh) for JMH benchmarks, which can access the main classes,
 * and a {@code jmh} task to run them.
 * <p>
 * The JMH version is taken from the {@code jmh_version} project property.
 */
public class JmhPlugin implements Plugin<Project> {
    public static final String SOURCE_SET_NAME = "jmh";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        var main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        var jmh = sourceSets.create(SOURCE_SET_NAME, sourceSet -> {
            sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(main.getOutput()));
            sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(main.getOutput()));
        });

        var configurations = project.getConfigurations();
        configurations.getByName(jmh.getImplementationConfigurationName())
                .extendsFrom(configurations.getByName(main.getImplementationConfigurationName()));
        configurations.getByName(jmh.getRuntimeOnlyConfigurationName())
                .extendsFrom(configurations.getByName(main.getRuntimeOnlyConfigurationName()));

        var jmhVersion = project.getProviders().gradleProperty("jmh_version");
        var dependencies = project.getDependencies();
        dependencies.addProvider(jmh.getImplementationConfigurationName(),
                jmhVersion.map(v -> "org.openjdk.jmh:jmh-core:" + v));
        dependencies.addProvider(jmh.getAnnotationProcessorConfigurationName(),
                jmhVersion.map(v -> "org.openjdk.jmh:jmh-generator-annprocess:" + v));

        project.getTasks().register("jmh", RunJmh.class, task -> {
            task.setGroup("benchmark");
            task.setDescription("Runs the JMH benchmarks.");
            task.setClasspath(jmh.getRuntimeClasspath());
        });
    }
}
