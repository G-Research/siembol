package uk.co.gresearch.siembol.configeditor.git;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.PullRequestService;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.siembol.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;

import java.io.IOException;
import java.util.List;

public class ReleasePullRequestService {
    private static final String BODY_TEMPLATE = "User %s would like to release %s version %d.";
    private static final String PR_STATE_OPEN = "open";
    private static final String MISSING_ARGUMENTS_MSG = "Missing arguments required for pull request service";

    private final RepositoryId repoId;
    private final PullRequestService service;
    private final String branchTo;

    private ReleasePullRequestService(Builder builder) {
        this.repoId = builder.repoId;
        this.service = builder.service;
        this.branchTo = builder.branchTo;
    }

    public ConfigEditorResult createPullRequest(ConfigInfo info) throws IOException {
        PullRequest request = new PullRequest();
        request.setBody(String.format(BODY_TEMPLATE,
                info.getCommitter(),
                info.getConfigInfoType().getReleaseName().toLowerCase(),
                info.getVersion()));

        request.setTitle(info.getCommitMessage());
        request.setHead(new PullRequestMarker().setLabel(info.getBranchName()));
        request.setBase(new PullRequestMarker().setLabel(branchTo));

        PullRequest response = service.createPullRequest(repoId, request);
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setPullRequestUrl(response.getHtmlUrl());
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    public ConfigEditorResult pendingPullRequest() throws IOException {
        List<PullRequest> requests = service.getPullRequests(repoId, PR_STATE_OPEN);

        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setPendingPullRequest(!requests.isEmpty());
        if (!requests.isEmpty()) {
            attributes.setPullRequestUrl(requests.get(0).getHtmlUrl());
        }
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    public static class Builder {
        private String uri;
        private String user;
        private String password;
        private String repoName;
        private RepositoryId repoId;
        private PullRequestService service;
        private String branchTo = GitRepository.MAIN_BRANCH;

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder repoName(String repoName) {
            this.repoName = repoName;
            return this;
        }

        public Builder credentials(String user, String password) {
            this.user = user;
            this.password = password;
            return this;
        }

        public ReleasePullRequestService build() {
            if (uri == null
                    || user == null
                    || repoName == null
                    || password == null
                    || repoName == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
            }

            repoId = RepositoryId.createFromId(repoName);

            GitHubClient client = GitHubClient.createClient(uri);
            client.setCredentials(user, password);
            service = new PullRequestService(client);

            return new ReleasePullRequestService(this);
        }
    }
}
