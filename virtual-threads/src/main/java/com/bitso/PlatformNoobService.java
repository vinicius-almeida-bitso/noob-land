package com.bitso;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/v1/platform/noob")
public class PlatformNoobService {

    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.MINUTES)
            .build();

    ObjectMapper objectMapper = new ObjectMapper();
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @GetMapping(path = "/one")
    public ResponseEntity<?> doNoobThingsPlatformThreadsOne() {

        var start = Instant.now().toEpochMilli();

        AtomicReference<String> responseString = new AtomicReference<>("");
        List<Future<String>> responses = new ArrayList<>();

        // thread per request style
        // Let's say we have 10 requests to make
        IntStream.range(0, 10).forEach(i -> {
            var response = executorService.submit(() -> request("http://localhost:8081/v1/fireblocks/account?vaultId=2&assetId=eth&workspace=hw&network=eth"));
            responses.add(response);
        });

        responses.forEach(responseFuture -> {
            try {
                var responseBody = "\n" + objectMapper.readValue(responseFuture.get(), Object.class);
                responseString.updateAndGet(v -> v + responseBody);
            } catch (IOException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        var end = Instant.now().toEpochMilli() - start;
        System.out.printf("doNoobThingsPlatformThreadsOne: timelapse=%s%n", end);
        
        return ResponseEntity.ok(responseString.get());
    }

    @GetMapping(path = "/two")
    public ResponseEntity<?> doNoobThingsPlatformThreadsTwo() {

        var start = Instant.now().toEpochMilli();

        AtomicReference<String> responseString = new AtomicReference<>("");
        List<String> responses = new ArrayList<>();

        // Let's say we have 10 requests to make
        IntStream.range(0, 10).forEach(i -> {
            Thread.ofPlatform().start(() -> {
                try {
                    var response = request("http://localhost:8081/v1/fireblocks/account?vaultId=2&assetId=eth&workspace=hw&network=eth");
                    responses.add(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        responses.forEach(response -> {
            try {
                var responseBody = "\n" + objectMapper.readValue(response, Object.class);
                responseString.updateAndGet(v -> v + responseBody);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        var end = Instant.now().toEpochMilli() - start;
        System.out.printf("doNoobThingsPlatformThreadsTwo: timelapse=%s%n", end);

        return ResponseEntity.ok(responseString.get());
    }

    public String request(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        var response = client.newCall(request).execute();
        var result = response.body().string();
        response.close();
        return result;
    }

//            var firstRequest = executor.submit(() -> request("http://localhost:8081/v1/fireblocks/account?vaultId=2&assetId=eth&workspace=hw&network=eth"));
//            var secondRequest = executor.submit(() -> request("http://localhost:8081/v1/fireblocks/account?vaultId=2&assetId=usdt&workspace=hw&network=eth"));
//            var thirdRequest = executor.submit(() -> request("http://localhost:8081/v1/fireblocks/account?vaultId=2&assetId=usdc&workspace=hw&network=eth"));
//            var fourthRequest = executor.submit(() -> request("http://localhost:8081/v1/fireblocks/account?vaultId=2&assetId=pepe&workspace=hw&network=eth"));
//
//            var response = ResponseEntity.ok("%s\n%s\n%s\n%s".formatted(
//                objectMapper.readValue(firstRequest.get().body().string(), Object.class),
//                objectMapper.readValue(secondRequest.get().body().string(), Object.class),
//                objectMapper.readValue(thirdRequest.get().body().string(), Object.class),
//                objectMapper.readValue(fourthRequest.get().body().string(), Object.class)));
//
//
//            var end = Instant.now().toEpochMilli() - start;
//
//            System.out.printf("timelapse=%s%n", end);
//
//            return response;

}
