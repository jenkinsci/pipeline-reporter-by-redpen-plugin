package org.jenkinsci.plugins.redpen.redpenservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractBuild;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jenkinsci.plugins.redpen.httpclient.HttpClientProvider;
import org.jenkinsci.plugins.redpen.models.CommentRequestModel;

import java.io.File;
import java.io.IOException;

public class RedpenService {

    private static RedpenService INSTANCE;
    private final OkHttpClient httpClient;
    private static final String BASE_PATH = "https://api.beta.redpen.work";

    private RedpenService() {
        final HttpClientProvider httpClientProvider = new HttpClientProvider();
        this.httpClient = httpClientProvider.httpClient();
    }

    public static RedpenService getRedpenInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RedpenService();
        }
        return INSTANCE;
    }

    public void addAttachment(AbstractBuild<?, ?> build, String issueKey, String token) throws IOException {
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", build.getLogFile()
                                .getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File(build.getLogFile()
                                        .getAbsolutePath())))
                .build();

        Request request = new Request.Builder()
                .url(String.format("%s/external/issues/%s/attachments", BASE_PATH, issueKey))
                .method("POST", body)
                .addHeader("Authorization", "JWT " + token)
                .build();

        this.httpClient.newCall(request)
                .execute();

    }

    public void addComment(AbstractBuild<?, ?> build, String issueKey, String token) {
        String logFileName = build.getLogFile()
                .getName();
        String comment = String.format("Log file for the failed build is [^%s] ", logFileName);

        ObjectMapper mapper = new ObjectMapper();
        MediaType json = MediaType.parse("application/json");
        try {
            CommentRequestModel commentRequestModel = new CommentRequestModel(comment);
            String bodyString = mapper.writeValueAsString(commentRequestModel);
            RequestBody body = RequestBody.create(json, bodyString);

            Request request = new Request.Builder()
                    .url(String.format("%s/external/issues/%s/comments", BASE_PATH, issueKey))
                    .method("POST", body)
                    .addHeader("Authorization", "JWT " + token)
                    .build();

            this.httpClient.newCall(request)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
