package appengbuild;

import appengbuild.crowdin.CrowdinTask;
import appengbuild.crowdin.DownloadFromCrowdin;
import appengbuild.crowdin.UploadSources;
import appengbuild.crowdin.UploadTranslations;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CrowdinPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        var tasks = project.getTasks();

        tasks.register("uploadToCrowdin", UploadSources.class, task -> {
            task.getLangFolder().set(project.getLayout().getProjectDirectory().dir("src/generated/resources/assets/ae2/lang"));
        });
        tasks.register("uploadTranslations", UploadTranslations.class, task -> {
            task.getLangFolder().set(project.getLayout().getProjectDirectory().dir("src/main/resources/assets/ae2/lang"));
        });
        tasks.register("downloadFromCrowdin", DownloadFromCrowdin.class, task -> {
            task.getLangFolder().set(project.getLayout().getProjectDirectory().dir("src/main/resources/assets/ae2/lang"));
        });

        tasks.withType(CrowdinTask.class).configureEach(task -> {
            task.getBaseUrl().convention("https://appliedenergistics2.crowdin.com/api/v2");
            task.getCrowdinToken().convention(project.getProviders().environmentVariable("CROWDIN_TOKEN"));
            task.getGitBranch().convention(project.getProviders().environmentVariable("GIT_BRANCH"));
            task.getProjectId().convention("1");
        });
    }
}
