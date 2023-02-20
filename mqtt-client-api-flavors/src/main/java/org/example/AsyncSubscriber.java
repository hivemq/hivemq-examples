package org.example;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class AsyncSubscriber {

    public static void main(final String @NotNull [] args) {
        final Mqtt5AsyncClient asyncClient = Mqtt5Client.builder()
                .identifier("async-subscriber")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildAsync();

        asyncClient.publishes(MqttGlobalPublishFilter.ALL, publish -> {
            System.out.println("Received publish with payload: " + new String(publish.getPayloadAsBytes(), UTF_8));
        });

        asyncClient.connect()
                .thenCompose(connAck -> {
                    System.out.println("Successfully connected!");
                    return asyncClient.subscribeWith().topicFilter("example/topic/#").send();
                }).thenRun(() -> {
                    System.out.println("Successfully subscribed!");
                }).exceptionally(throwable -> {
                    System.out.println("Something went wrong!");
                    throwable.printStackTrace();
                    return null;
                });
    }
}
