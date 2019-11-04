package uk.co.gresearch.nortem.configeditor.configstore;

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
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorAttributes;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorFile;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorFileHistoryItem;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GitRepository implements Closeable {
    public static final String MAIN_BRANCH = "master";
    private static final String TESTCASE_DISABLED = "Testcases are disabled for this repository";
    private final CredentialsProvider credentialsProvider;
    private final Git git;
    private final Path rulesPath;
    private final Path testCasePath;
    private final String repoUri;
    private final ConfigEditorFile.ContentType contentType;

    private static String readFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path), UTF_8);
    }

    private GitRepository(Builder builder) {
        credentialsProvider = builder.credentialsProvider;
        git = builder.git;
        rulesPath = builder.rulesPath;
        testCasePath = builder.testCasesPath;
        repoUri = builder.repoUri;
        contentType = builder.contentType;
    }

    public ConfigEditorResult transactCopyAndCommit(
            ConfigInfo configInfo) throws GitAPIException, IOException {
        Path currentPath = configInfo.getConfigInfoType() == ConfigInfoType.TEST_CASE ? testCasePath : rulesPath;
        if (currentPath == null) {
            throw new IllegalStateException(TESTCASE_DISABLED);
        }

        git.pull()
                .setCredentialsProvider(credentialsProvider)
                .call();

        if (!MAIN_BRANCH.equals(configInfo.getBranchName())) {
            git.branchCreate().setName(configInfo.getBranchName()).call();
            git.checkout().setName(configInfo.getBranchName()).call();
        }

        if (configInfo.shouldCleanDirectory()) {
            FileUtils.cleanDirectory(currentPath.toFile());
        }

        for (Map.Entry<String, String> file : configInfo.getFilesContent().entrySet()) {
            Path filePath = Paths.get(currentPath.toString(), file.getKey());
            Files.write(filePath, file.getValue().getBytes());
        }

        git.add()
                .addFilepattern(currentPath.getFileName().toString())
                .call();

        git.commit()
                .setAll(true)
                .setAuthor(configInfo.getCommitter(), configInfo.getCommitterEmail())
                .setMessage(configInfo.getCommitMessage())
                .call();

        git.push()
                .setCredentialsProvider(credentialsProvider)
                .call();

        ConfigEditorResult result = getFiles(currentPath);

        if (!MAIN_BRANCH.equals(configInfo.getBranchName())) {
            git.checkout().setName(MAIN_BRANCH).call();
        }
        return result;
    }

    public ConfigEditorResult getConfigs() throws IOException, GitAPIException {
        return getFiles(rulesPath);
    }

    public ConfigEditorResult getTestCases() throws IOException, GitAPIException {
        if (testCasePath == null) {
            throw new IllegalStateException(TESTCASE_DISABLED);
        }

        return getFiles(testCasePath);
    }

    private ConfigEditorResult getFiles(Path path) throws IOException, GitAPIException {
        git.pull()
                .setCredentialsProvider(credentialsProvider)
                .call();

        Map<String, ConfigEditorFile> files = new HashMap<>();
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
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
        attr.setFiles(files.values().stream().collect(Collectors.toList()));
        return new ConfigEditorResult(ConfigEditorResult.StatusCode.OK, attr);
    }

    public String getRepoUri() {
        return repoUri;
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
        private Path rulesPath;
        private Path testCasesPath;
        private String rulesDirectory = "";
        private String testCaseDirectory;
        private CredentialsProvider credentialsProvider;
        private Git git;
        private ConfigEditorFile.ContentType contentType = ConfigEditorFile.ContentType.RAW_JSON_STRING;

        Builder repoName(String repoName) {
            this.repoName = repoName;
            return this;
        }

        Builder gitUrl(String gitUrl) {
            this.gitUrl = gitUrl;
            return this;
        }

        Builder rulesDirectory(String rulesDirectory) {
            this.rulesDirectory = rulesDirectory;
            return this;
        }

        Builder testCaseDirectory(String testCaseDirectory) {
            this.testCaseDirectory = testCaseDirectory;
            return this;
        }

        Builder repoFolder(String repoFolder) {
            this.repoFolder = repoFolder;
            return this;
        }

        Builder contentType(ConfigEditorFile.ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        Builder credentials(String userName, String password) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(userName, password);
            return this;
        }

        GitRepository build() throws GitAPIException, IOException {
            if (repoName == null
                    || gitUrl == null
                    || repoFolder == null
                    || credentialsProvider == null) {
                throw new IllegalArgumentException("Not provided required properties");
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

            rulesPath = Paths.get(repoFolder, rulesDirectory);
            if (testCaseDirectory != null) {
                testCasesPath = Paths.get(repoFolder, testCaseDirectory);
            }

            if (git == null || !repoFolderDir.exists()) {
                throw new IllegalStateException("Error during git rules repo initialisation");
            }

            return new GitRepository(this);
        }
    }
}
