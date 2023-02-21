package org.example;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class AsyncPublisher {

    public static void main(final String @NotNull [] args) {
        final var asyncClient = Mqtt5Client.builder()
                .identifier("async-publisher")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildAsync();

        asyncClient.connect() //
                .thenCompose(connAck -> {
                    System.out.println("Successfully connected!");
                    final var futures = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        final Mqtt5Publish publish = Mqtt5Publish.builder()
                                .topic("example/topic/async")
                                .payload(("example #" + i).getBytes(UTF_8))
                                .qos(MqttQos.AT_LEAST_ONCE)
                                .build();
                        final var future = asyncClient.publish(publish);
                        futures.add(future);
                    }
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                }).thenCompose(unused -> {
                    System.out.println("Successfully published!");
                    return asyncClient.disconnect();
                }).thenRun(() -> {
                    System.out.println("Successfully disconnected!");
                }).exceptionally(throwable -> {
                    System.out.println("Something went wrong!");
                    return null;
                });
    }
}
