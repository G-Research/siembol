package uk.co.gresearch.siembol.configeditor.git;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import uk.co.gresearch.siembol.configeditor.common.ConfigInfo;
import uk.co.gresearch.siembol.configeditor.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GitRepository implements Closeable {
    private static final String MISSING_ARGUMENTS_MSG = "Missing arguments required for git repository initialisation";
    private static final String ERROR_INIT_MSG = "Error during git repository initialisation";
    private static final String ERROR_PUSH_MSG = "Error during git repository push with message: %s";
    private static final String GIT_REPO_DIRECTORY_URL_FORMAT = "%s/%s/tree/%s/%s";
    private final CredentialsProvider credentialsProvider;
    private final Git git;
    private final String gitUrl;
    private final String repoName;
    private final String repoFolder;
    private final String repoUri;
    private final String defaultBranch;
    private final ConfigEditorFile.ContentType contentType;

    private static String readFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path), UTF_8);
    }

    private void createDirectoryIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private GitRepository(Builder builder) {
        credentialsProvider = builder.credentialsProvider;
        git = builder.git;
        repoUri = builder.repoUri;
        contentType = builder.contentType;
        repoFolder = builder.repoFolder;
        gitUrl = builder.gitUrl;
        repoName = builder.repoName;
        defaultBranch = builder.defaultBranch;
    }

    public ConfigEditorResult transactCopyAndCommit(
            ConfigInfo configInfo,
            String directory,
            Function<String, Boolean> fileNameFilter) throws GitAPIException, IOException {
        final boolean isDefaultBranch = !configInfo.getBranchName().isPresent()
                || defaultBranch.equals(configInfo.getBranchName().get());

        git.pull()
                .setCredentialsProvider(credentialsProvider)
                .call();

        if (!isDefaultBranch) {
            git.branchCreate().setName(configInfo.getBranchName().get()).call();
            git.checkout().setName(configInfo.getBranchName().get()).call();
        }

        Path currentPath = Paths.get(repoFolder, directory);
        createDirectoryIfNotExists(currentPath);
        if (configInfo.shouldCleanDirectory()) {
            FileUtils.cleanDirectory(currentPath.toFile());
        }

        for (Map.Entry<String, Optional<String>> file : configInfo.getFilesContent().entrySet()) {
            Path filePath = Paths.get(currentPath.toString(), file.getKey());
            if (file.getValue().isPresent()) {
                Files.write(filePath, file.getValue().get().getBytes());
            } else {
                Files.delete(filePath);
            }
        }

        git.add()
                .addFilepattern(directory)
                .call();

        git.commit()
                .setAll(true)
                .setAuthor(configInfo.getCommitter(), configInfo.getCommitterEmail())
                .setMessage(configInfo.getCommitMessage())
                .call();

        Iterable<PushResult> pushResults = git.push()
                .setCredentialsProvider(credentialsProvider)
                .call();

        for (PushResult pushResult: pushResults) {
            for (RemoteRefUpdate update: pushResult.getRemoteUpdates()) {
                if (!update.getStatus().equals(RemoteRefUpdate.Status.OK)) {
                    return ConfigEditorResult.fromMessage(ConfigEditorResult.StatusCode.ERROR,
                            String.format(ERROR_PUSH_MSG, update.getMessage()));
                }
            }
        }

        ConfigEditorResult result = getFiles(directory, fileNameFilter);
        if (!isDefaultBranch) {
            git.checkout().setName(defaultBranch).call();
        }
        return result;
    }


    public ConfigEditorResult getFiles(String directory) throws IOException, GitAPIException {
        return getFiles(directory, x -> true);
    }

    public ConfigEditorResult getFiles(String directory,
                                       Function<String, Boolean> fileNameFilter) throws IOException, GitAPIException {
        git.pull()
                .setCredentialsProvider(credentialsProvider)
                .call();

        Path path = Paths.get(repoFolder, directory);
        createDirectoryIfNotExists(path);
        Map<String, ConfigEditorFile> files = new HashMap<>();
        try (Stream<Path> paths = Files.walk(path)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(x -> fileNameFilter.apply(x.getFileName().toString()))
                    .forEach(x -> {
                        try {
                            files.put(x.getFileName().toString(),
                                    new ConfigEditorFile(x.getFileName().toString(), readFile(x), contentType));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        Iterable<RevCommit> commits = git.log().setRevFilter(RevFilter.NO_MERGES).call();
        try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            df.setRepository(git.getRepository());
            df.setDiffComparator(RawTextComparator.DEFAULT);
            for (RevCommit commit : commits) {
                if (commit.getParentCount() == 0) {
                    //NOTE: we skip init commit
                    continue;
                }

                String author = commit.getAuthorIdent().getName();
                int commitTime = commit.getCommitTime();
                RevCommit parent = commit.getParent(0);

                List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
                for (DiffEntry diff : diffs) {
                    int linesAdded = 0, linesRemoved = 0;
                    int lastSlashIndex = diff.getNewPath().lastIndexOf('/');
                    String fileName = lastSlashIndex < 0
                            ? diff.getNewPath()
                            : diff.getNewPath().substring(lastSlashIndex + 1);
                    if (!files.containsKey(fileName)) {
                        continue;
                    }

                    for (Edit edit : df.toFileHeader(diff).toEditList()) {
                        linesRemoved += edit.getEndA() - edit.getBeginA();
                        linesAdded += edit.getEndB() - edit.getBeginB();
                    }

                    ConfigEditorFileHistoryItem historyItem = new ConfigEditorFileHistoryItem();
                    historyItem.setAuthor(author);
                    historyItem.setTimestamp(commitTime);
                    historyItem.setAddedLines(linesAdded);
                    historyItem.setRemoved(linesRemoved);
                    files.get(fileName).getFileHistory().add(historyItem);
                }
            }
        }

        ConfigEditorAttributes attr = new ConfigEditorAttributes();
        attr.setFiles(new ArrayList<>(files.values()));
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
    }

    public String getRepoUri() {
        return repoUri;
    }

    public String getDirectoryUrl(String directory) {
        return String.format(GIT_REPO_DIRECTORY_URL_FORMAT, gitUrl, repoName, defaultBranch, directory);
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    @Override
    public void close() {
        git.close();
    }

    public static class Builder {
        private static final String GIT_REPO_URL_FORMAT = "%s/%s.git";
        private String repoName;
        private String repoUri;
        private String gitUrl;
        private String repoFolder;
        private String defaultBranch;
        private CredentialsProvider credentialsProvider;
        private Git git;
        private ConfigEditorFile.ContentType contentType = ConfigEditorFile.ContentType.RAW_JSON_STRING;

        public Builder repoName(String repoName) {
            this.repoName = repoName;
            return this;
        }

        public Builder gitUrl(String gitUrl) {
            this.gitUrl = gitUrl;
            return this;
        }

        public Builder repoFolder(String repoFolder) {
            this.repoFolder = repoFolder;
            return this;
        }

        public Builder credentials(String userName, String password) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(userName, password);
            return this;
        }

        public GitRepository build() throws GitAPIException, IOException {
            if (repoName == null
                    || gitUrl == null
                    || repoFolder == null
                    || credentialsProvider == null) {
                throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
            }

            File repoFolderDir = new File(repoFolder);
            if (repoFolderDir.exists()) {
                FileUtils.cleanDirectory(repoFolderDir);
            } else {
                repoFolderDir.mkdir();
            }

            repoUri = String.format(GIT_REPO_URL_FORMAT, gitUrl, repoName);

            git = Git.cloneRepository()
                    .setCredentialsProvider(credentialsProvider)
                    .setURI(repoUri)
                    .setDirectory(repoFolderDir)
                    .call();

            defaultBranch = git.getRepository().getBranch();
            if (git == null || defaultBranch == null || !repoFolderDir.exists()) {
                throw new IllegalStateException(ERROR_INIT_MSG);
            }

            return new GitRepository(this);
        }
    }
}
