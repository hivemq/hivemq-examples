package org.example;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class BlockingPublisher {

    public static void main(final String @NotNull [] args) {
        final Mqtt5BlockingClient blockingClient = Mqtt5Client.builder()
                .identifier("blocking-publisher")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildBlocking();

        blockingClient.connect();
        System.out.println("Successfully connected!");

        for (int i = 0; i < 10; i++) {
            final Mqtt5Publish publish = Mqtt5Publish.builder()
                    .topic("example/topic/blocking")
                    .payload(("example #" + i).getBytes(UTF_8))
                    .build();
            blockingClient.publish(publish);
        }
        System.out.println("Successfully published!");
        blockingClient.disconnect();
        System.out.println("Successfully disconnected!");
    }
}
