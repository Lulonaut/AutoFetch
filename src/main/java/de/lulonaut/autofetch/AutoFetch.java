package de.lulonaut.autofetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import de.lulonaut.autofetch.settings.AppSettingsState;
import git4idea.GitUtil;
import git4idea.fetch.GitFetchSupport;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AutoFetch implements StartupActivity, StartupActivity.DumbAware {
    private ExecutorService executorService = Executors.newFixedThreadPool(100);

    @Override
    public void runActivity(@NotNull Project project) {
        executorService.submit(() -> {
            while (true) {
                if (AppSettingsState.getInstance().getEnabledStatus()) {
                    //stop if the project is no longer open
                    if (project.isDisposed()) {
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

                //wait out the delay
                try {
                    Thread.sleep(AppSettingsState.getInstance().getAutoFetchDelay() * 60000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
