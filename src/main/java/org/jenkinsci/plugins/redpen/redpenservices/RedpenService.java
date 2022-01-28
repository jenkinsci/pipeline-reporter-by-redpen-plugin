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
    private static final String BASE_PATH = "http://localhost:8081";

    private RedpenService() {
        this.httpClient = HttpClients.createDefault();
    }

    public static RedpenService getRedpenInstance() {
        if (instance == null) {
            instance = new RedpenService();
        }
        return instance;
    }

    private String getNewFile(AbstractBuild<?, ?> build, String path) {
        return path + "_" + build.getDisplayName() + "_" + build.getResult();
    }

    public CloseableHttpResponse addAttachment(AbstractBuild<?, ?> build, String issueKey, String token, String path)
            throws IOException {

        File file = new File(path);
        File file2 = new File(getNewFile(build, path));
        FileUtils.copyFile(file, file2);
        HttpPost post = new HttpPost(String.format("%s/external/jenkins/issues/%s/attachments", BASE_PATH, issueKey));
        FileBody fileBody = new FileBody(file2, ContentType.DEFAULT_BINARY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        post.addHeader("Authorization", "JWT " + token);
        post.setEntity(entity);

        return this.httpClient.execute(post);
    }

    public void addComment(AbstractBuild<?, ?> build, String issueKey, String token) {

        try {

            String comment = String.format("Build %s", build.getDisplayName());
            StringBuilder json = new StringBuilder();

            json.append("{ \"comment\": \"").append("[^").append(getNewFile(build, build.getLogFile().getName()))
                    .append("] ").append(comment).append("\" }");

            HttpPost request = new HttpPost(String.format("%s/external/jenkins/issues/%s/comment", BASE_PATH,
                    issueKey));
            request.addHeader("Authorization", "JWT " + token);
            request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            StringEntity stringEntity = new StringEntity(json.toString());
            request.setEntity(stringEntity);

            try (CloseableHttpResponse response = this.httpClient.execute(request)) {

                EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getJWT(String widgetId) {

        try {

            HttpPost request = new HttpPost(String.format("%s/widget/authentication", BASE_PATH));

            StringBuilder json = new StringBuilder();

            json.append("{ \"widgetId\": \"" + widgetId + "\" }");

            request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            request.addHeader("site-origin", "https://www.xbox.com");
            StringEntity stringEntity = new StringEntity(json.toString());
            request.setEntity(stringEntity);

            try (CloseableHttpResponse response = this.httpClient.execute(request)) {

                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
