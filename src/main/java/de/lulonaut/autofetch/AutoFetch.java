package de.lulonaut.autofetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.concurrency.AppExecutorUtil;
import de.lulonaut.autofetch.settings.AppSettingsState;
import git4idea.GitUtil;
import git4idea.fetch.GitFetchSupport;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoFetch implements StartupActivity, StartupActivity.DumbAware {
    private static final Logger LOG = Logger.getInstance(AutoFetch.class);

    @Override
    public void runActivity(@NotNull Project project) {
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
            if (AppSettingsState.getInstance().getEnabledStatus()) {
                //stop if the project is no longer open
                LOG.warn("run");
                if (project.isDisposed()) {
                    LOG.warn("exit");
                    throw new RuntimeException();
                }

                //get all connected git repos and sort out repos with no remotes
                List<GitRepository> repositories = GitUtil.getRepositories(project).stream().filter(repo -> !repo.getRemotes().isEmpty()).collect(Collectors.toList());
                if (!repositories.isEmpty()) {
                    //fetch the remaining repos with remotes
                    GitFetchSupport gitFetchSupport = GitFetchSupport.fetchSupport(project);
                    if (!gitFetchSupport.isFetchRunning()) {
                        gitFetchSupport.fetchAllRemotes(repositories);
                    }
                }
            }
        }, 0, AppSettingsState.getInstance().getAutoFetchDelay(), TimeUnit.MINUTES);
    }
}
