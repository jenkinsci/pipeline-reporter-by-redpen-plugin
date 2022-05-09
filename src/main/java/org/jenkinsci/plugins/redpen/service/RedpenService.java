package org.jenkinsci.plugins.redpen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.redpen.constant.Constants;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class RedpenService {
    private static final Logger LOGGER = Logger.getLogger(RedpenService.class.getName());

    private static RedpenService instance;
    private final CloseableHttpClient httpClient;
    public static final String UPLOAD_ATTACHMENT = "Upload Attachment";
    public static final String ADD_COMMENT = "add comment";


    private RedpenService() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Singleton object of RedpenService class
     * @return instance of RedpenService
     */
    public static RedpenService getRedpenInstance() {
        if (instance == null) {
            instance = new RedpenService();
        }
        return instance;
    }

    /**
     * Upload Attachment on Jira Issue
     * @param issueKey : Jira issue key [TEST-1, TP-2]
     * @param jwtToken : JWT Token
     * @param file : File to upload on Jira issue
     * @return Status of attachment uploaded successfully or not. TRUE = uploaded successfully, FALSE = upload fail
     */
    public boolean addAttachment(String issueKey, String jwtToken, File file, String fileName) {
        try {
            String path = String.format("%s/external/jenkins/issues/%s/attachments", Constants.BASE_PATH, issueKey);

            HttpPost post = new HttpPost(path);

            FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY, fileName);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("file", fileBody);
            HttpEntity entity = builder.build();

            post.addHeader(HttpHeaders.AUTHORIZATION, String.format("JWT %s", jwtToken));
            post.addHeader("client-id", Constants.CLIENT_ID);

            post.setEntity(entity);

            try (CloseableHttpResponse response = this.httpClient.execute(post)) {
                return responseHandler(response, UPLOAD_ATTACHMENT);
            }

        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
        return false;
    }

    /**
     * This Method does Response handling
     * @param response : CloseableHttpResponse
     * @param message : message for failure scenario
     * @return TRUE = response success, FALSE = response failure
     */
    private boolean responseHandler(CloseableHttpResponse response, String message) throws IOException {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            EntityUtils.toString(response.getEntity());
            return true;
        } else {
            String warning = String.format("Unable to %s pm status %s message %s", message,
                    response.getStatusLine().getStatusCode(), response.getEntity().toString());
            LOGGER.warning(warning);
            return false;
        }
    }

    /**
     * Add Comment on Jira issue
     * @param issueKey : Jira issue key [TEST-1, TP-2]
     * @param jwtToken : JWT Token
     * @param comment : Comment string
     * @param uploadedFileNames : Attachment list
     */
    public void addComment(String issueKey, String jwtToken, String comment, List<String> uploadedFileNames) {
        try {
            String path = String.format("%s/external/jenkins/issues/%s/comment", Constants.BASE_PATH, issueKey);
            String commentBody = getCommentBody(comment, uploadedFileNames);

            HttpPost request = new HttpPost(path);

            request.addHeader(HttpHeaders.AUTHORIZATION, String.format("JWT %s", jwtToken));
            request.addHeader("client-id", Constants.CLIENT_ID);
            request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            StringEntity stringEntity = new StringEntity(commentBody);
            request.setEntity(stringEntity);

            try (CloseableHttpResponse response = this.httpClient.execute(request)) {
                responseHandler(response, ADD_COMMENT);
            }
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private String getCommentBody(String comment, List<String> attachments) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode node = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();

        for (String a : attachments) {
            arrayNode.add(a);
        }
        node.put("comment", comment);
        node.put("attachments", arrayNode);

        return node.toString();
    }
}
