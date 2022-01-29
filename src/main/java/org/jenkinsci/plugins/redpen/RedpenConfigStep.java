package org.jenkinsci.plugins.redpen;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.redpen.ghpr.GithubPrHelper;
import org.jenkinsci.plugins.redpen.jwt.JWTUtility;
import org.jenkinsci.plugins.redpen.redpenservices.RedpenService;
import org.jenkinsci.plugins.redpen.secrets.SecretRetriever;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RedpenConfigStep extends Recorder {
    private String serviceConnectionId;

    @DataBoundConstructor
    public RedpenConfigStep(String serviceConnectionId) {
        this.serviceConnectionId = serviceConnectionId;
    }

    public String getServiceConnectionId() {
        return serviceConnectionId;
    }

    public void setServiceConnectionId(String serviceConnectionId) {
        this.serviceConnectionId = serviceConnectionId;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        Result result = build.getResult();

        // If the build status is not SUCCESS then
        // call the redpen and add comment with log file as an attachment in the issue.
        if (result != null && result.isWorseThan(Result.SUCCESS)) {
            GithubPrHelper githubPrHelper = new GithubPrHelper();
            String issueKey = githubPrHelper.getIssueKeyFromPR(build);
            SecretRetriever secretRetriever = new SecretRetriever();
            Optional<String> mayBeKey = secretRetriever.getSecretFor("PRIVATE_KEY_CONTENT");

            RedpenService redpenService = RedpenService.getRedpenInstance();
            if (mayBeKey.isPresent()) {
                try {
                    String jwtToken = JWTUtility.getJWTToken(mayBeKey.get(), this.serviceConnectionId);
                    File file = new File(build.getLogFile().getAbsolutePath());
                    File file2 = new File(redpenService.getNewFile(build, build.getLogFile().getAbsolutePath()));
                    FileUtils.copyFile(file, file2);

                    redpenService.addAttachment(build, issueKey, jwtToken, file2);

                    String currentDir = System.getProperty("user.dir");

                    String filePathLog = currentDir + "/work/workspace/" + build.getProject().getName() + "/src/e2e/logs";
                    String filePathReport = currentDir + "/work/workspace/" + build.getProject().getName() + "/src/e2e/reports/master-report.pdf";
                    String commentString = String.format("Build %s Result %s", build.getDisplayName(), build.getResult());
                    List<String> logFiles = attachLogFiles(build, filePathLog, issueKey, jwtToken);
                    List<String> reportFiles = attachLogFiles(build, filePathReport, issueKey, jwtToken);

                    List<String> allFiles = new ArrayList<>();
                    allFiles.addAll(logFiles);
                    allFiles.addAll(reportFiles);
                    allFiles.add(redpenService.getNewFile(build, build.getLogFile().getName()));

                    StringBuilder comment = new StringBuilder();
                    for (String fileName: allFiles) {
                        comment.append(String.format("[^%s]", fileName));
                    }
                    comment.append(commentString);

                    redpenService.addComment(build, issueKey, jwtToken, comment.toString());
                    return true;

                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return true;
    }

    private List<String> attachLogFiles(AbstractBuild<?, ?> build, String filePath, String issueKey, String jwtToken) throws IOException {
        File e2eLogs = new File(filePath);
        RedpenService redpenService = RedpenService.getRedpenInstance();
        List<String> fileNames = new ArrayList<>();

        List<File> files = listFilesForFolder(e2eLogs);
        for (File file1 : files) {
            String absolutePath = file1.getAbsolutePath();

            Path filePathAbsolute = Paths.get(absolutePath);
            BasicFileAttributes attr = Files.readAttributes(filePathAbsolute, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();

            FileTime from = FileTime.from(build.getTime().toInstant());

            int after = from.compareTo(fileTime);

            if (after > 0) {
                redpenService.addAttachment(build, issueKey, jwtToken, file1);
                fileNames.add(file1.getName());
            }
        }
        return fileNames;
    }

    public List<File> listFilesForFolder(File folder) {
        List<File> files = new ArrayList<>();
        File[] files1 = folder.listFiles();
        if (folder.exists() && files1 != null) {

            for (File fileEntry : files1) {
                if (fileEntry.isDirectory()) {
                    files.addAll(listFilesForFolder(fileEntry));
                } else {
                    files.add(fileEntry);
                }
            }
        }
        if (folder.exists() && !folder.isDirectory()) {
            files.add(folder);
        }

        return files;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class aClass) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Redpen ServiceConnectionId";
        }
    }
}
