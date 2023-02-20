package org.example;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class AsyncPublisher {

    public static void main(final String @NotNull [] args) {
        final Mqtt5AsyncClient asyncClient = Mqtt5Client.builder()
                .identifier("async-publisher")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildAsync();

        asyncClient.connect() //
                .thenCompose(connAck -> {
                    System.out.println("Successfully connected!");
                    var futures = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        futures.add(asyncClient.publishWith()
                                .topic("example/topic/async")
                                .payload(("example #" + i).getBytes(UTF_8))
                                .send());
                    }
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                }).thenCompose(unused -> {
                    System.out.println("Successfully published!");
                    return asyncClient.disconnect();
                }).thenRun(() -> {
                    System.out.println("Successfully disconnected!");
                }).exceptionally(throwable -> {
                    System.out.println("Something went wrong!");
                    throwable.printStackTrace();
                    return null;
                });
    }
}
