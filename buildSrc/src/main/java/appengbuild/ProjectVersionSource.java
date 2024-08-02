package appengbuild;

import org.gradle.api.GradleException;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Largely adapted from https://github.com/neoforged/GradleUtils
 */
public abstract class ProjectVersionSource implements ValueSource<String, ProjectVersionSource.ProjectVersionSourceParams> {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectVersionSource.class);

    private static final Pattern[] VALID_VERSION_TAGS = {
            Pattern.compile("neoforge/v(\\d+\\.\\d+\\.\\d+)(|[+-].*)?"),
            Pattern.compile("forge/v(\\d+\\.\\d+\\.\\d+)(|[+-].*)?"),
            Pattern.compile("fabric/v(\\d+\\.\\d+\\.\\d+)(|[+-].*)?"),
            Pattern.compile("v(\\d+\\.\\d+\\.\\d+)(|[+-].*)?")
    };

    @Override
    public String obtain() {
        try {
            return detectVersion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted");
        } catch (Exception e) {
            LOG.error("Failed to determine Project version from Git.", e);
            return "0.0.0";
        }
    }

    private String detectVersion() throws InterruptedException, IOException {
        var gitVersion = git("--version");
        if (!gitVersion.contains("git version")) {
            throw new RuntimeException("Git version command did not return a git version: " + gitVersion);
        }

        return calculate();
    }

    private String calculate() throws IOException, InterruptedException {
        var describe = findTag();

        // Bumps the minor version up by one (1.0.5 -> 1.0.6)
        var triplet = describe.version.split("\\.");
        triplet[triplet.length - 1] = String.valueOf(Integer.parseUnsignedInt(triplet[triplet.length - 1]) + 1);
        var version = String.join(".", triplet);

        // Appends a -pre.<offset>
        version += "-alpha." + describe.offset();

        var branchSuffix = getBranchSuffix();
        if (branchSuffix != null) {
            version += "+" + branchSuffix;
        }

        return version;
    }

    private DescribeOutput findTag() throws IOException, InterruptedException {
        int trackedCommitCount = 0;
        String currentRev = "HEAD";

        while (true) {
            String described = git("describe", "--long", "--tags", currentRev);
            if (described == null) {
                throw new GradleException("Cannot calculate the project version without a previous Git tag. Did you forget to run \"git fetch --tags\"?");
            }

            // Describe (long) output is "<tag>-<offset>-g<commit>"
            String[] describeSplit = rsplit(described, "-", 2);

            var tag = describeSplit[0];
            trackedCommitCount += Integer.parseUnsignedInt(describeSplit[1]);

            for (var versionTagPattern : VALID_VERSION_TAGS) {
                var m = versionTagPattern.matcher(tag);
                if (m.matches()) {
                    var version = m.group(1);
                    return new DescribeOutput(tag, trackedCommitCount, version);
                }
            }

            // Because the Git CLI doesn't provide the equivalent to '--exclude', we have to manually go about this
            // by searching the parent of the current tag, which is why we track the commit count

            // nth ancestor selector (~2 meaning grandparent)
            currentRev = tag + "~1";
            // This accounts for the commit at the current tag (which is skipped due to using tag's parent)
            trackedCommitCount += 1;
        }
    }

    public static String[] rsplit(String input, String del, int limit) {
        if (input == null) return null;
        List<String> lst = new ArrayList<>();
        int x = 0, idx;
        String tmp = input;
        while ((idx = tmp.lastIndexOf(del)) != -1 && (limit == -1 || x++ < limit)) {
            lst.add(0, tmp.substring(idx + del.length()));
            tmp = tmp.substring(0, idx);
        }
        lst.add(0, tmp);
        return lst.toArray(String[]::new);
    }

    record DescribeOutput(String tag, int offset, String version) {
    }

    @Nullable
    private String getBranchSuffix() throws IOException, InterruptedException {
        String headOutput = git("symbolic-ref", "-q", "HEAD");
        String longBranch = headOutput.isEmpty() ? null : headOutput;

        String branch = longBranch != null ? shortenRefName(longBranch) : "";
        if (getParameters().getDefaultBranches().get().contains(branch)) {
            // Branch is exempted from suffix
            return null;
        }

        // Convert GH pull request refs names (pulls/<#>/head) to a smaller format, without the /head
        if (branch.startsWith("pulls/")) {
            branch = "pr" + rsplit(branch, "/", 1)[1];
        }

        return branch.replaceAll("[\\\\/]", "-");
    }

    private String shortenRefName(String refName) {
        return refName.replaceFirst("^refs/heads/", "");
    }

    private static String git(String... args) throws InterruptedException, IOException {
        var combinedArgs = new ArrayList<String>(1 + args.length);
        combinedArgs.add("git");
        Collections.addAll(combinedArgs, args);
        var process = new ProcessBuilder(combinedArgs).start();

        // We provide no STDIN to the process
        process.getOutputStream().close();

        // Combined output is easier for debugging problems
        var combinedOutput = new StringBuilder();
        // Stdout is easier for parsing output in the successful case
        var stdout = new StringBuilder();

        var stdoutReader = startLineReaderThread(process.inputReader(), line -> {
            stdout.append(line);
            synchronized (combinedOutput) {
                combinedOutput.append(line);
            }
        });
        var stderrReader = startLineReaderThread(process.errorReader(), line -> {
            synchronized (combinedOutput) {
                combinedOutput.append(line);
            }
        });

        var exitCode = process.waitFor();

        stderrReader.join();
        stdoutReader.join();

        if (exitCode != 0) {
            throw new RuntimeException("Failed running " + combinedArgs + ". Exit Code " + exitCode
                                       + ", Output: " + combinedOutput);
        }

        return stdout.toString().trim();
    }

    private static Thread startLineReaderThread(BufferedReader reader, Consumer<String> lineHandler) {
        var stdoutReader = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineHandler.accept(line);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("Failed to close process output stream.", e);
                }
            }
        });
        stdoutReader.setUncaughtExceptionHandler((t, e) -> LOG.error("Failed to read output of external process.", e));
        stdoutReader.setDaemon(true);
        stdoutReader.start();
        return stdoutReader;
    }

    public abstract static class ProjectVersionSourceParams implements ValueSourceParameters {
        @Input
        @Optional
        public abstract ListProperty<String> getDefaultBranches();
    }

    private static class NativeEncodingHolder {
        static final Charset charset;

        static {
            var nativeEncoding = System.getProperty("native.encoding");
            if (nativeEncoding == null) {
                throw new IllegalStateException("The native.encoding system property is not available, but should be since Java 17!");
            }
            charset = Charset.forName(nativeEncoding);
        }
    }
}
