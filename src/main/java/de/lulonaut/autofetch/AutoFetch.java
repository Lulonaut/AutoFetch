package de.lulonaut.autofetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import de.lulonaut.autofetch.settings.AppSettingsState;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.fetch.GitFetchSupport;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoFetch implements StartupActivity, StartupActivity.DumbAware {
    private static final Logger LOG = Logger.getInstance(AutoFetch.class);

    @Override
    public void runActivity(@NotNull Project project) {
        // Wait for the initialization to avoid trying to issue a git fetch before any git logic is initialized
        ProjectLevelVcsManager.getInstance(project).runAfterInitialization(() -> {
            // Do the initial fetch
            fetchAllRepos(project);

            // For subsequent fetches, use the AppExecutorUtil
            final CompletableFuture<ScheduledFuture<?>> gitFetchFuture = new CompletableFuture<>();
            final ScheduledFuture<?> task = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
                if (AppSettingsState.getInstance().getEnabledStatus()) {
                    // Stop if the project is no longer open
                    if (project.isDisposed()) {
                        gitFetchFuture.cancel(true);
                        return;
                    }

                    fetchAllRepos(project);
                }
            }, AppSettingsState.getInstance().getAutoFetchDelay(), AppSettingsState.getInstance().getAutoFetchDelay(), TimeUnit.MINUTES);
        });
    }

    private void fetchAllRepos(@NotNull Project project) {
        List<GitRepository> repositories = GitUtil.getRepositories(project).stream().filter(repo -> !repo.getRemotes().isEmpty()).collect(Collectors.toList());
        if (!repositories.isEmpty()) {
            try {
                GitVcs.runInBackground(new Task.Backgroundable(project, "Fetching all remotes") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        GitFetchSupport gitFetchSupport = GitFetchSupport.fetchSupport(project);
                        if (!gitFetchSupport.isFetchRunning()) {
                            gitFetchSupport.fetchAllRemotes(repositories);
                        }
                    }
                });

            } catch (IllegalStateException e) {
                // Skip this fetch
                LOG.warn("IllegalStateException while fetching");
                LOG.warn(e);
            }
        }
    }
}
