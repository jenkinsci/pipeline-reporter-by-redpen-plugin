package org.jenkinsci.plugins.redpen.service;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.redpen.auth.JWTUtility;
import org.jenkinsci.plugins.redpen.constant.Constants;
import org.jenkinsci.plugins.redpen.models.ParameterModel;
import org.jenkinsci.plugins.redpen.models.TestFrameWork;
import org.jenkinsci.plugins.redpen.util.FileUtils;
import org.jenkinsci.plugins.redpen.util.PathUtils;

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
import java.util.logging.Logger;

public class RedpenJenkinsCore {
    private static final Logger LOGGER = Logger.getLogger(RedpenJenkinsCore.class.getName());

    /**
     * Attach logs and generated artifacts of job, and Add status of job in Jira issue comment.
     * @param parameter : ParameterModel
     */
    public void doPerform(ParameterModel parameter) {
        String issueKey = parameter.getIssueKey();
        String jwtToken = getJWT(parameter);

        String jobStarted = String.format("Redpen Plugin Detect Failure on build %s followed by jira ticket %s", parameter.getBuildNumber(), issueKey);
        LOGGER.info(jobStarted);

        if (!StringUtils.isBlank(jwtToken)) {
            try {
                List<String> allFiles = addAttachment(parameter, jwtToken);
                addComment(parameter, jwtToken, allFiles);

                String jobCompleted = String.format("Redpen Plugin Detect Failure on build %s followed by jira ticket %s", parameter.getBuildNumber(), issueKey);
                LOGGER.info(jobCompleted);

            } catch (IOException e) {
                LOGGER.warning(String.format("Something went wrong during Redpen Plugin Task %s", e.getMessage()));
            }
        }
    }

    /**
     * Attach build log file and added Configured files in Jira issue.
     * @param parameter : ParameterModel
     * @param jwtToken : JWT Token
     * @return  List of Attached file name
     */
    private List<String> addAttachment(ParameterModel parameter, String jwtToken) throws IOException {
        RedpenService redpenService = RedpenService.getRedpenInstance();

        List<String> allFiles = new ArrayList<>();

        String issueKey = parameter.getIssueKey();
        String projectName = parameter.getProjectName();
        Instant buildTriggerTime = parameter.getBuildTriggerTime();
        String basePath = PathUtils.getPath(String.format("%s/workspace/%s", getCurrentDirPath(), projectName));

        File copiedLogFile = getLogFile(parameter);
        boolean attachmentUploaded = redpenService.addAttachment(issueKey, jwtToken, copiedLogFile);

        if (attachmentUploaded) {
            allFiles.add(copiedLogFile.getName());
            LOGGER.info("File : " + copiedLogFile.getName() + " is attached");
        }

        List<String> unitTestUploadedFiles = uploadFilesFromSelectedTestFrameWork(issueKey, jwtToken, parameter.getUnitTestFrameWork(), parameter.getUnitTestFrameWorkPath(), basePath, buildTriggerTime);
        allFiles.addAll(unitTestUploadedFiles);

        List<String> e2eTestUploadedFiles = uploadFilesFromSelectedTestFrameWork(issueKey, jwtToken, parameter.getE2eTestFrameWork(), parameter.getE2eTestFrameWorkPath(), basePath, buildTriggerTime);
        allFiles.addAll(e2eTestUploadedFiles);

        List<String> coverageUploadedFiles = uploadFilesFromSelectedTestFrameWork(issueKey, jwtToken, parameter.getCoverageFrameWork(), parameter.getCoverageFrameWorkPath(), basePath, buildTriggerTime);
        allFiles.addAll(coverageUploadedFiles);

        String[] logDir = parameter.getLogFileLocation().trim().split(",");

        for (String s : logDir) {
            String trimPath = s.trim();
            if (!StringUtils.isBlank(trimPath)) {
                String logPath = String.format("%s%s", basePath, trimPath);
                List<String> reportFiles = attachLogFiles(buildTriggerTime, logPath, issueKey, jwtToken, true);
                allFiles.addAll(reportFiles);
            }
        }

        allFiles.forEach(file -> LOGGER.info(String.format("File : %s is attached", file)));
        return allFiles;
    }

    /**
     * Add Comment on Jira issue
     * @param parameter : ParameterModel
     * @param jwtToken : JWT Token
     * @param allFiles : Attached fileNames
     */
    private void addComment(ParameterModel parameter, String jwtToken, List<String> allFiles) {
        RedpenService redpenService = RedpenService.getRedpenInstance();
        String jobURL = getJobUrl(parameter);
        String comment = getGeneratedComment(parameter, jobURL);

        redpenService.addComment(parameter.getIssueKey(), jwtToken, comment, allFiles);
    }

    /**
     * Create Current Job URL
     *
     * @param parameter : ParameterModel
     * @return jobURL : "http:<jenkins-server-url>/job/<project-name>/<job-number>"
     */
    private String getJobUrl(ParameterModel parameter) {
        return String.format("%s%s/%s/%s", parameter.getRootURL(), "job", parameter.getProjectName(), parameter.getBuildNumber());
    }

    /**
     * Generate Comment for Jira issue
     * @param parameter : ParameterModel
     * @param jobURL : Jenkins job URL
     * @return generated Comment for Jira RTE.
     */
    private String getGeneratedComment(ParameterModel parameter, String jobURL) {
        String displayName = parameter.getDisplayName();
        String result = parameter.getResult();
        Instant buildTriggerTime = parameter.getBuildTriggerTime();

        return String.format("Build [%s|%s] Result {color:red}*%s*{color} Time %s", displayName, jobURL, result, buildTriggerTime);
    }

    /**
     * Get JWT
     * @param parameter : ParameterModel
     * @return JWT Token to make API call
     */
    private String getJWT(ParameterModel parameter) {
        return JWTUtility.getJWTToken(parameter.getSecret(), parameter.getUserEmail(), parameter.getUserPassword());
    }

    /**
     * Get Work Directory of Jenkins
     * @return work directory path
     */
    private String getCurrentDirPath() {
        String currentDir = System.getenv().get(Constants.JENKINS_HOME);

        if (currentDir == null) {
            currentDir = String.format("%s%s", System.getProperty("user.dir"), "/work");
        }

        return currentDir;
    }

    /**
     * Get current job's log file and copies log file
     * @param parameter : ParameterModel
     * @return returns copied log file with file name log_buildName_buildStatus
     */
    private File getLogFile(ParameterModel parameter) throws IOException {

        String logAbsolutePath = parameter.getLogAbsolutePath();
        String displayName = parameter.getDisplayName();
        String result = parameter.getResult();

        // build log's absolute file path
        File buildLogFile = new File(logAbsolutePath);
        String newLogFilePath = FileUtils.getNewFile(logAbsolutePath, displayName, result);
        // create new file from build log's to log_<buildId>_<status>
        File copiedLogFile = new File(newLogFilePath);

        org.apache.commons.io.FileUtils.copyFile(buildLogFile, copiedLogFile);

        return copiedLogFile;
    }

    /**
     * Upload generated artifacts of job in Jira issue as per given configuration in Jenkins Job Configuration.
     * @param issueKey : Jira issue key [TEST-1, TP-2]
     * @param jwtToken : JWT Token
     * @param frameWork : TestFrameWork
     * @param frameWorkPath : Updated File Path for selected test framework
     * @param basePath : workspace path
     * @param buildTriggerTime : build trigger time
     * @return List of attached file name
     */
    private List<String> uploadFilesFromSelectedTestFrameWork(String issueKey, String jwtToken, String frameWork, String frameWorkPath, String basePath, Instant buildTriggerTime) throws IOException {
        List<String> allFiles = new ArrayList<>();
        Optional<TestFrameWork> availableInList = RedpenJenkinsCore.isAvailableInList(frameWork);

        if (availableInList.isPresent() && availableInList.get().getValue() != null) {
            String fileRelativePath = !StringUtils.isBlank(frameWorkPath) ? frameWorkPath : availableInList.get().getPath();
            String fileAbsolutePath = String.format("%s%s", basePath, fileRelativePath);

            List<String> reportFiles = attachLogFiles(buildTriggerTime, PathUtils.getPath(fileAbsolutePath), issueKey, jwtToken, false);
            allFiles.addAll(reportFiles);
        }

        return allFiles;
    }

    /**
     * Get Selected Test framework's default log path
     * @param key : Test framework's key
     * @return : log file path as per selected test framework
     */
    public static Optional<TestFrameWork> isAvailableInList(String key) {
        return Constants.TEST_FRAME_WORKS.stream().filter(a -> a.getValue().equals(key)).findFirst();
    }

    /**
     * Attach log file and generated artifacts if that file generated after build start.
     * @param buildStartTime : Build start time
     * @param filePath : File path [it can be folder path or direct absolute filepath]
     * @param issueKey : Jira issue key [TEST-1, TP-2]
     * @param jwtToken : JWT Token
     * @param isMandatory : If this parameter is true then this method will upload attachment even if file is not generated after build start.
     * @return List of attached file name
     */
    private List<String> attachLogFiles(Instant buildStartTime, String filePath, String issueKey, String jwtToken, Boolean isMandatory) throws IOException {
        RedpenService redpenService = RedpenService.getRedpenInstance();
        List<String> fileNames = new ArrayList<>();

        File logs = new File(filePath);
        List<File> files = FileUtils.listFilesForFolder(logs);

        for (File file : files) {
            String absolutePath = file.getAbsolutePath();
            Path filePathAbsolute = Paths.get(absolutePath);
            BasicFileAttributes attr = Files.readAttributes(filePathAbsolute, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            FileTime from = FileTime.from(buildStartTime);

            int after = from.compareTo(fileTime);

            if (after < 0 || Boolean.TRUE.equals(isMandatory)) {
                boolean attachmentUploaded = redpenService.addAttachment(issueKey, jwtToken, file);
                if (attachmentUploaded) {
                    fileNames.add(file.getName());
                }
            }
        }
        return fileNames;
    }
}
