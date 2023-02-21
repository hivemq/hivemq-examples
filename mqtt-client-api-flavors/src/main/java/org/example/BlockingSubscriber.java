package org.example;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class BlockingSubscriber {

    public static void main(final String @NotNull [] args) throws Exception {
        final var blockingClient = Mqtt5Client.builder()
                .identifier("blocking-subscriber")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildBlocking();

        try (final var publishes = blockingClient.publishes(MqttGlobalPublishFilter.ALL)) {
            try {
                blockingClient.connect();
            } catch (final Exception e) {
                System.out.println("Error while connecting!");
                throw e;
            }
            System.out.println("Successfully connected!");
            try {
                blockingClient.subscribeWith().topicFilter("example/topic/#").send();
            } catch (final Exception e) {
                System.out.println("Error while subscribing!");
                throw e;
            }
            System.out.println("Successfully subscribed!");

            while (true) {
                final Mqtt5Publish publish = publishes.receive();
                System.out.println("Received publish with payload: " + new String(publish.getPayloadAsBytes(), UTF_8));
            }
        }
    }
}
