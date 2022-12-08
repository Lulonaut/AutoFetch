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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoFetch implements StartupActivity, StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        final CompletableFuture<ScheduledFuture<?>> gitFetchFuture = new CompletableFuture<>();
        final ScheduledFuture<?> task = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
            if (AppSettingsState.getInstance().getEnabledStatus()) {
                //stop if the project is no longer open
                if (project.isDisposed()) {
                    gitFetchFuture.cancel(true);
                    return;
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
        gitFetchFuture.complete(task);
    }
}
