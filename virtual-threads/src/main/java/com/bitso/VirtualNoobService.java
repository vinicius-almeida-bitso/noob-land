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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/v1/virtual/noob")
public class VirtualNoobService {

    OkHttpClient client = new OkHttpClient();
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Thread-per-request style with virtual threads
     */
    @GetMapping
    public ResponseEntity<?> doNoobThingsVirtualThreads() {
        
        var start = Instant.now().toEpochMilli();
        
        // thread per request style
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        
            AtomicReference<String> responseString = new AtomicReference<>("");
            
            // Let's say we have 10 requests to make
            IntStream.range(0, 10).forEach(i -> {
                var response = executor.submit(() -> request("http://localhost:8081/v1/fireblocks/account?vaultId=2&assetId=eth&workspace=hw&network=eth"));
                try {
                    var responseBody = "\n" + objectMapper.readValue(response.get().body().string(), Object.class);
                    responseString.updateAndGet(v -> v + responseBody);
                } catch (IOException | InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            var end = Instant.now().toEpochMilli() - start;
            System.out.printf("timelapse=%s%n", end);
            return ResponseEntity.ok(responseString.get());
            
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
            
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    
    }

    public Response request(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request).execute();
    }

}
