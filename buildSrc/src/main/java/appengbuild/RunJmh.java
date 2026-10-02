package appengbuild;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs JMH benchmarks found on the classpath. The main class must accept the JMH command line options; it defaults to
 * the JMH command line runner. Every run writes its results to {@link #getResultsDirectory()}, as
 * {@code <timestamp>-<revision>.json} (JSON) and {@code <timestamp>-<revision>.txt} (text), where the revision is
 * given by {@link #getRevision()}. This allows comparing the results of runs on different revisions.
 * <p>
 * Every run starts with an empty task-specific temporary directory, which is used as the working directory and, via
 * {@code java.io.tmpdir}, for temporary files. JMH passes both on to its forked JVMs.
 */
@DisableCachingByDefault(because = "Benchmark results are not reproducible")
public abstract class RunJmh extends JavaExec {
    public RunJmh() {
        getMainClass().set("org.openjdk.jmh.Main");
        getOutputs().upToDateWhen(task -> false);
        setWorkingDir(getTemporaryDir());
        systemProperty("java.io.tmpdir", getJavaTempDir().getAbsolutePath());
        getResultsDirectory().convention(getProject().getLayout().getBuildDirectory().dir("reports/jmh"));

        getWarmupForks().convention(0);
        getWarmupIterations().convention(1);
        getForks().convention(1);
        getIterations().convention(
                getProject().getProviders().gradleProperty("jmh.iterations").map(Integer::parseInt).orElse(1));
        getThreads().convention(1);
        getExtraArgs().convention(getProject().getProviders().gradleProperty("jmh.args")
                .map(value -> Arrays.stream(value.trim().split("\\s+")).filter(arg -> !arg.isEmpty()).toList())
                .orElse(List.of()));
    }

    @Input
    public abstract Property<Integer> getWarmupForks();

    @Input
    public abstract Property<Integer> getWarmupIterations();

    @Input
    public abstract Property<Integer> getForks();

    /**
     * Number of measurement iterations. Defaults to the {@code jmh.iterations} project property.
     */
    @Input
    public abstract Property<Integer> getIterations();

    @Input
    public abstract Property<Integer> getThreads();

    /**
     * Profilers to enable, in JMH's {@code -prof} syntax (i.e. {@code name:options}).
     */
    @Input
    public abstract ListProperty<String> getProfilers();

    /**
     * Additional arguments passed to the JMH runner as-is, i.e. a benchmark name pattern or {@code -prof gc}.
     * Defaults to the whitespace-separated {@code jmh.args} project property.
     */
    @Input
    public abstract ListProperty<String> getExtraArgs();

    /**
     * Directory that receives the results of every run: machine-readable benchmark results in JSON format, and a
     * human-readable log of the run, to which JMH's console output is copied. Defaults to {@code build/reports/jmh}.
     */
    @OutputDirectory
    public abstract DirectoryProperty getResultsDirectory();

    /**
     * Identifies the revision of the benchmarked code in the names of the result files, i.e. {@code 3ca129526} or
     * {@code 3ca129526-dirty}.
     */
    @Input
    public abstract Property<String> getRevision();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    private File getJavaTempDir() {
        return new File(getTemporaryDir(), "tmp");
    }

    @TaskAction
    @Override
    public void exec() {
        // Start with a clean slate, without leftovers of the previous run (i.e. game directories)
        getFileSystemOperations().delete(spec -> spec.delete(getTemporaryDir()));
        if (!getJavaTempDir().mkdirs()) {
            throw new UncheckedIOException(new IOException("Failed to create " + getJavaTempDir()));
        }

        var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        var baseName = timestamp + "-" + getRevision().get();
        var resultsDirectory = getResultsDirectory().get();
        var jsonResultFile = resultsDirectory.file(baseName + ".json").getAsFile();
        var textResultFile = resultsDirectory.file(baseName + ".txt").getAsFile();

        var args = new ArrayList<String>();
        // Fail the task if any benchmark throws, instead of just reporting it
        args.add("-foe");
        args.add("true");
        args.add("-wf");
        args.add(getWarmupForks().get().toString());
        args.add("-f");
        args.add(getForks().get().toString());
        args.add("-wi");
        args.add(getWarmupIterations().get().toString());
        args.add("-i");
        args.add(getIterations().get().toString());
        args.add("-t");
        args.add(getThreads().get().toString());
        args.add("-rf");
        args.add("json");
        args.add("-rff");
        args.add(jsonResultFile.getAbsolutePath());
        for (var profiler : getProfilers().get()) {
            args.add("-prof");
            args.add(profiler);
        }
        args.addAll(getExtraArgs().get());
        args(args);

        // JMH writes its output either to the console or to a file (-o), so copy the console output to the file instead
        try (var fileOut = new FileOutputStream(textResultFile)) {
            setStandardOutput(new TeeOutputStream(System.out, fileOut));
            super.exec();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            // Don't leave incomplete results behind, which would be picked up by reports. The log explains the failure.
            jsonResultFile.delete();
            throw e;
        }
    }

    private static final class TeeOutputStream extends OutputStream {
        private final OutputStream first;
        private final OutputStream second;

        TeeOutputStream(OutputStream first, OutputStream second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void write(int b) throws IOException {
            first.write(b);
            second.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            first.write(b, off, len);
            second.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            first.flush();
            second.flush();
        }
    }
}
