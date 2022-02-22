package org.jenkinsci.plugins.redpen.redpenservices;

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
import org.jenkinsci.plugins.redpen.models.Constants;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class RedpenService {

    private static RedpenService instance;
    private final CloseableHttpClient httpClient;
    private static final String BASE_PATH = Constants.BASE_PATH;
    private static final String CLIENT_ID = Constants.CLIENT_ID;

    private RedpenService() {
        this.httpClient = HttpClients.createDefault();
    }

    public static RedpenService getRedpenInstance() {
        if (instance == null) {
            instance = new RedpenService();
        }
        return instance;
    }

    public String getNewFile(String path, String displayName, String result) {
        return String.format("%s_%s_%s", path, displayName, result);
    }

    public void addAttachment(String issueKey, String token, File file)
            throws IOException {
        try {
            HttpPost post = new HttpPost(
                    String.format("%s/external/jenkins/issues/%s/attachments", BASE_PATH, issueKey));
            FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("file", fileBody);
            HttpEntity entity = builder.build();

            post.addHeader(HttpHeaders.AUTHORIZATION, String.format("JWT %s", token));
            post.addHeader("client-id", CLIENT_ID);
            post.setEntity(entity);

            try (CloseableHttpResponse response = this.httpClient.execute(post)) {
                responseHandler(response, "Upload Attachment");
            }

        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private void responseHandler(CloseableHttpResponse response, String message) throws IOException {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            EntityUtils.toString(response.getEntity());
        } else {

            LOGGER.warning(String.format("Unable to %s pm status %s message %s", message,
                    response.getStatusLine().getStatusCode(), response.getEntity().toString()));
        }
    }

    public void addComment(String issueKey, String token, String comment) {
        try {

            String commentBody = String.format("{ \"comment\": \"%s\" }", comment);

            HttpPost request = new HttpPost(
                    String.format("%s/external/jenkins/issues/%s/comment", BASE_PATH, issueKey));
            request.addHeader(HttpHeaders.AUTHORIZATION, String.format("JWT %s", token));
            request.addHeader("client-id", CLIENT_ID);

            request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            StringEntity stringEntity = new StringEntity(commentBody);
            request.setEntity(stringEntity);

            try (CloseableHttpResponse response = this.httpClient.execute(request)) {
                responseHandler(response, "add comment");
            }
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private static final Logger LOGGER = Logger.getLogger(RedpenService.class.getName());
}
