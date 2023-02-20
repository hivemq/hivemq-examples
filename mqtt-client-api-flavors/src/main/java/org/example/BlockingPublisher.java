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

        try {
            blockingClient.connect();
        } catch (final Exception e) {
            System.out.println("Error while connecting!");
            throw e;
        }
        System.out.println("Successfully connected!");

        for (int i = 0; i < 10; i++) {
            try {
                final Mqtt5Publish publish = Mqtt5Publish.builder()
                        .topic("example/topic/blocking")
                        .payload(("example #" + i).getBytes(UTF_8))
                        .build();
                blockingClient.publish(publish);
            } catch (final Exception e) {
                System.out.println("Error while publishing!");
                throw e;
            }
        }
        System.out.println("Successfully published!");
        try {
            blockingClient.disconnect();
        } catch (final Exception e) {
            System.out.println("Error while disconnecting!");
            throw e;
        }
        System.out.println("Successfully disconnected!");
    }
}
