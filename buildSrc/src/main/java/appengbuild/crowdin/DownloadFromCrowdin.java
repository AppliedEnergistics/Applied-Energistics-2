package appengbuild.crowdin;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public abstract class DownloadFromCrowdin extends CrowdinTask {
    /**
     * Folder where en_us.json is
     */
    @InputDirectory
    public abstract DirectoryProperty getLangFolder();

    @TaskAction
    public void run() throws Exception {
        withCrowdin(crowdin -> {
            crowdin.downloadTranslations(getGitBranch().get(), getLangFolder().get().getAsFile().toPath());
        });
    }
}
