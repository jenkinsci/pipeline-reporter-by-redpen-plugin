package org.jenkinsci.plugins.redpen.redpenservices;

import hudson.model.AbstractBuild;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
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
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;

public class RedpenService {

    private static RedpenService instance;
    private final CloseableHttpClient httpClient;
    private static final String BASE_PATH = "https://api.dev.redpen.work";

    private RedpenService() {
        this.httpClient = HttpClients.createDefault();
    }

    public static RedpenService getRedpenInstance() {
        if (instance == null) {
            instance = new RedpenService();
        }
        return instance;
    }

    public String getNewFile(AbstractBuild<?, ?> build, String path) {
        return path + "_" + build.getDisplayName() + "_" + build.getResult();
    }

    public void addAttachment(AbstractBuild<?, ?> build, String issueKey, String token, File file)
            throws IOException {
        try {
            HttpPost post = new HttpPost(
                    String.format("%s/external/jenkins/issues/%s/attachments", BASE_PATH, issueKey));
            FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("file", fileBody);
            HttpEntity entity = builder.build();

            post.addHeader("Authorization", "JWT " + token);
            post.setEntity(entity);

            try (CloseableHttpResponse response = this.httpClient.execute(post)) {
                EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addComment(AbstractBuild<?, ?> build, String issueKey, String token, String comment) {

        try {

            String commentBody = String.format("{ \"comment\": \"%s\" }", comment);

            HttpPost request = new HttpPost(
                    String.format("%s/external/jenkins/issues/%s/comment", BASE_PATH, issueKey));
            request.addHeader("Authorization", "JWT " + token);
            request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            StringEntity stringEntity = new StringEntity(commentBody);
            request.setEntity(stringEntity);

            try (CloseableHttpResponse response = this.httpClient.execute(request)) {
                EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
