package com.example.playground;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SyncAsyncExamples {

    @Test
    public void SyncExample() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.naver.com"))
            .build();

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }

    @Test
    public void ASyncExample() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.naver.com"))
            .build();

        CompletableFuture<HttpResponse<String>> future =
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(Thread.currentThread() + "다음 작업");

        future.thenApply(HttpResponse::body);

        future.thenApply(HttpResponse::body)
            .thenAccept(body -> System.out.println(Thread.currentThread() + body))
            .join();
    }


}
