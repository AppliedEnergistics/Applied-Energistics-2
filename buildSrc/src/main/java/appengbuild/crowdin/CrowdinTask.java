package appengbuild.crowdin;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

public abstract class CrowdinTask extends DefaultTask {
    @Input
    public abstract Property<String> getBaseUrl();

    @Internal
    public abstract Property<String> getCrowdinToken();

    @Input
    public abstract Property<String> getGitBranch();

    @Input
    public abstract Property<String> getProjectId();

    protected final void withCrowdin(CrowdinConsumer action) throws Exception {
        try (var crowdin = new Crowdin(getBaseUrl().get(), getCrowdinToken().get())) {
            action.execute(crowdin);
        }
    }

    @FunctionalInterface
    public interface CrowdinConsumer {
        void execute(Crowdin crowdin) throws Exception;
    }
}
