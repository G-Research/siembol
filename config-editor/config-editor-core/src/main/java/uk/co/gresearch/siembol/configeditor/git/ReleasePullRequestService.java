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
/**
 * An object for interaction with a GitHub API
 *
 *
 * <p>This class is used for interaction with a GitHub API in order to create a pull request and
 * checks for opened pull request in the repository.
 *
 * @author  Marian Novotny
 */
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

    /**
     * Creates a pull request
     * @param info metadata about the pull request
     * @return the config editor result with pull request status
     * @throws IOException
     */
    public ConfigEditorResult createPullRequest(ConfigInfo info) throws IOException {
        PullRequest request = new PullRequest();
        request.setBody(String.format(BODY_TEMPLATE,
                info.getCommitter(),
                info.getConfigInfoType().getReleaseName().toLowerCase(),
                info.getVersion()));

        request.setTitle(info.getCommitMessage());
        request.setHead(new PullRequestMarker().setLabel(info.getBranchName().get()));
        request.setBase(new PullRequestMarker().setLabel(branchTo));

        PullRequest response = service.createPullRequest(repoId, request);
        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setPullRequestUrl(response.getHtmlUrl());
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    /**
     * Gets info about pending pull request in the repository
     * @return the config editor result with a pending pull request flag
     * @throws IOException
     */
    public ConfigEditorResult pendingPullRequest() throws IOException {
        List<PullRequest> requests = service.getPullRequests(repoId, PR_STATE_OPEN);

        ConfigEditorAttributes attributes = new ConfigEditorAttributes();
        attributes.setPendingPullRequest(!requests.isEmpty());
        if (!requests.isEmpty()) {
            attributes.setPullRequestUrl(requests.get(0).getHtmlUrl());
        }
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attributes);
    }

    /**
     * A builder for a git repository
     *
     * @author  Marian Novotny
     */
    public static class Builder {
        private String uri;
        private String user;
        private String password;
        private String repoName;
        private RepositoryId repoId;
        private PullRequestService service;
        private String branchTo;

        /**
         * Sets GitHub url
         * @param uri a url to git server
         * @return this builder
         */
        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Sets the repository name
         * @param repoName the name of teh repository
         * @return this builder
         */
        public Builder repoName(String repoName) {
            this.repoName = repoName;
            return this;
        }

        /**
         * Sets the credentials for the GitHub repository
         * @param user the name of the user
         * @param password password or PAT
         * @return this builder
         */
        public Builder credentials(String user, String password) {
            this.user = user;
            this.password = password;
            return this;
        }

        /**
         * Sets the branch name for PR to be merged into it.
         * @param branchTo the name of the branch
         * @return this builder
         */
        public Builder branchTo(String branchTo) {
            this.branchTo = branchTo;
            return this;
        }

        /**
         * Builds the release pull request service
         * @return the pull request service built from the builder state
         *
         */
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
