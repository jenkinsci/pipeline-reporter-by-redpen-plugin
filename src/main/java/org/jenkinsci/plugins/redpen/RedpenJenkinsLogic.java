package org.jenkinsci.plugins.redpen;

import hudson.model.Run;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.redpen.jwt.JWTUtility;
import org.jenkinsci.plugins.redpen.models.Constants;
import org.jenkinsci.plugins.redpen.models.ParameterModel;
import org.jenkinsci.plugins.redpen.models.TestFrameWork;
import org.jenkinsci.plugins.redpen.redpenservices.RedpenService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class RedpenJenkinsLogic {
    public void doPerform(ParameterModel parameter)
            throws IOException {

        String issueKey = parameter.getIssueKey();
        String logAbsolutePath = parameter.getLogAbsolutePath();
        String displayName = parameter.getDisplayName();
        String result = parameter.getResult();
        String projectName = parameter.getProjectName();
        Instant buildTriggerTime = parameter.getBuildTriggerTime();


        RedpenService redpenService = RedpenService.getRedpenInstance();
        String jwtToken = JWTUtility.getJWTToken(parameter.getSecret());
        String newLogFilePath = redpenService.getNewFile(logAbsolutePath, displayName, result);
        // build log's absolute file path
        File buildLogFile = new File(logAbsolutePath);
        // create new file from build log's to log_<buildId>_<status>
        File copiedLogFile = new File(newLogFilePath);
        FileUtils.copyFile(buildLogFile, copiedLogFile);

        redpenService.addAttachment(issueKey, jwtToken, copiedLogFile);

        String currentDir = System.getenv().get(Constants.JENKINS_HOME);

        if (currentDir == null) {
            currentDir = String.format("%s/%s", System.getProperty("user.dir"), "work");
        }
        List<String> allFiles = new ArrayList<>();
        String basePath = String.format("%s/workspace/%s", currentDir, projectName);

        List<String> unitTestUploadedFiles = uploadFilesFromSelectedTestFrameWork(issueKey, jwtToken, parameter.getUnitTestFrameWork(), basePath, buildTriggerTime);
        List<String> e2eTestUploadedFiles = uploadFilesFromSelectedTestFrameWork(issueKey, jwtToken, parameter.getE2eTestFrameWork(), basePath, buildTriggerTime);
        List<String> coverageUploadedFiles = uploadFilesFromSelectedTestFrameWork(issueKey, jwtToken, parameter.getCoverageFrameWork(), basePath, buildTriggerTime);
        allFiles.addAll(unitTestUploadedFiles);
        allFiles.addAll(e2eTestUploadedFiles);
        allFiles.addAll(coverageUploadedFiles);

        // jobURL : "http:<jenkins-server-url>/job/<project-name>/<job-number>"
        String jobURL = String.format("%s%s/%s/%s", Jenkins.get().getRootUrl(), "job", projectName, parameter.getBuildNumber());


        String[] logDir = parameter.getLogFileLocation().trim().split(",");
        for (String s : logDir) {
            String trimPath = s.trim();
            if (!StringUtils.isBlank(trimPath)) {
                String logPath = String.format("%s%s", basePath, trimPath);
                List<String> reportFiles = attachLogFiles(buildTriggerTime, logPath, issueKey, jwtToken);
                allFiles.addAll(reportFiles);
            }
        }

        String commentString = String.format("Build [%s|%s] Result {color:red}*%s*{color} Time %s", displayName, jobURL,
                result,
                buildTriggerTime);

        allFiles.add(copiedLogFile.getName());

        StringBuilder comment = new StringBuilder();
        for (String fileName : allFiles) {
            comment.append(String.format("[^%s]", fileName));
        }
        comment.append(commentString);

        redpenService.addComment(issueKey, jwtToken, comment.toString());
    }

    private List<String> uploadFilesFromSelectedTestFrameWork (String issueKey, String jwtToken, String frameWork, String basePath, Instant buildTriggerTime) throws IOException {
        List<String> allFiles = new ArrayList<>();
        Optional<TestFrameWork> availableInList = isAvailableInList(frameWork);
        if (availableInList.isPresent() && availableInList.get().getValue() != null) {
            List<String> reportFiles = attachLogFiles(buildTriggerTime, String.format("%s%s", basePath, availableInList.get().getPath()),
                    issueKey, jwtToken);
            allFiles.addAll(reportFiles);
        }
        return allFiles;
    }

    private Optional<TestFrameWork> isAvailableInList(String key) {
        return Constants.TEST_FRAME_WORKS.stream().filter(a -> a.getValue().equals(key)).findFirst();
    }


    private List<String> attachLogFiles(Instant buildStartTime, String filePath, String issueKey, String jwtToken)
            throws IOException {
        File logs = new File(filePath);
        RedpenService redpenService = RedpenService.getRedpenInstance();
        List<String> fileNames = new ArrayList<>();

        List<File> files = listFilesForFolder(logs);
        for (File file : files) {
            String absolutePath = file.getAbsolutePath();

            Path filePathAbsolute = Paths.get(absolutePath);
            BasicFileAttributes attr = Files.readAttributes(filePathAbsolute, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();

            FileTime from = FileTime.from(buildStartTime);

            int after = from.compareTo(fileTime);

            if (after < 0) {
                redpenService.addAttachment(issueKey, jwtToken, file);
                fileNames.add(file.getName());
            }
        }
        return fileNames;
    }

    public List<File> listFilesForFolder(File folder) {
        List<File> files = new ArrayList<>();
        File[] directory = folder.listFiles();
        if (folder.exists() && directory != null) {

            for (File fileEntry : directory) {
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

    public ParameterModel getParameterModel(String secret, String issueKey, Run build, RedpenJobProperty redpenPluginJobProperties) {
        return ParameterModel.builder()
                .secret(secret)
                .issueKey(issueKey)
                .logAbsolutePath(build.getLogFile().getAbsolutePath())
                .displayName(build.getDisplayName())
                .result(String.valueOf(build.getResult()))
                .projectName(build.getParent().getName())
                .buildNumber(build.getSearchUrl())
                .buildTriggerTime(build.getTime().toInstant())
                .e2eTestFrameWork(redpenPluginJobProperties.getE2eTestFrameWork())
                .unitTestFrameWork(redpenPluginJobProperties.getUnitTestFrameWork())
                .coverageFrameWork(redpenPluginJobProperties.getCoverageFrameWork())
                .logFileLocation(redpenPluginJobProperties.getLogFileLocation()).build();
    }
}
