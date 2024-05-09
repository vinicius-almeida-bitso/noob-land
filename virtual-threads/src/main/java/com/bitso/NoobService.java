package com.bitso;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/v1/noob")
public class NoobService {

    OkHttpClient client = new OkHttpClient();

    /**
     * Thread-per-request style with virtual threads
     */
    @GetMapping
    public ResponseEntity<?> doNoobThingsVirtualThreads() throws ExecutionException, InterruptedException {
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            
            var firstRequest = executor.submit(() -> request("http://localhost:8081"));
            var secondRequest = executor.submit(() -> request("http://localhost:8081"));
            var thirdRequest = executor.submit(() -> request("http://localhost:8081"));
            var fourthRequest = executor.submit(() -> request("http://localhost:8081"));
            
            return ResponseEntity.ok("%s-%s-%s-%s".formatted(
                firstRequest.get().toString(),
                secondRequest.get().toString(),
                thirdRequest.get().toString(),
                fourthRequest.get().toString()));
            
        } catch (ExecutionException | InterruptedException exception) {
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
