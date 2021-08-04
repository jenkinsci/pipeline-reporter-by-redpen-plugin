package org.jenkinsci.plugins.redpen.httpclient;

import com.google.inject.Provides;
import okhttp3.OkHttpClient;

import java.time.Duration;

public class HttpClientProvider {
    private final OkHttpClient httpClient;

    public HttpClientProvider() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(5000))
                .readTimeout(Duration.ofMillis(5000))
                .writeTimeout(Duration.ofMillis(5000))
                .build();
    }

    @Provides
    public OkHttpClient httpClient() {
        return httpClient;
    }
}
