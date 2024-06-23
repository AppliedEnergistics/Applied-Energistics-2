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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Largely adapted from https://github.com/neoforged/GradleUtils
 */
public abstract class ProjectVersionSource implements ValueSource<String, ProjectVersionSource.ProjectVersionSourceParams> {
    private static final Pattern[] VALID_VERSION_TAGS = {
            Pattern.compile("neoforge/v(\\d+\\.\\d+\\.\\d+)(|[+-].*)?"),
            Pattern.compile("forge/v(\\d+\\.\\d+\\.\\d+)(|[+-].*)?"),
            Pattern.compile("fabric/v(\\d+\\.\\d+\\.\\d+)(|[+-].*)?"),
            Pattern.compile("v(\\d+\\.\\d+\\.\\d+)(|[+-].*)?")
    };

    private static final Logger LOG = LoggerFactory.getLogger(ProjectVersionSource.class);

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

        var tag = calculate();
        System.out.println(tag);
        return "";
    }

    private String calculate() throws IOException, InterruptedException {
        var describe = findTag();

        // Bumps the minor version up by one (1.0.5 -> 1.0.6)
        var triplet = describe.version.split("\\.");
        triplet[triplet.length - 1] = String.valueOf(Integer.parseUnsignedInt(triplet[triplet.length - 1]) + 1);
        var version = String.join(".", triplet);

        // Appends a -pre.<offset>
        version += "-pre." + describe.offset();

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

        var reader = process.inputReader(StandardCharsets.UTF_8);
        String line;
        var result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        var exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed running " + combinedArgs);
        }

        return result.toString();
    }

    public abstract static class ProjectVersionSourceParams implements ValueSourceParameters {
        @Input
        @Optional
        public abstract ListProperty<String> getDefaultBranches();
    }
}
