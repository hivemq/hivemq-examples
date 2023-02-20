package org.example;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient.Mqtt5Publishes;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class BlockingSubscriber {

    public static void main(final String @NotNull [] args) throws Exception {
        final Mqtt5BlockingClient blockingClient = Mqtt5Client.builder()
                .identifier("blocking-subscriber")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildBlocking();

        try (final Mqtt5Publishes publishes = blockingClient.publishes(MqttGlobalPublishFilter.ALL)) {
            blockingClient.connect();
            System.out.println("Successfully connected!");
            blockingClient.subscribeWith().topicFilter("example/topic/#").send();
            System.out.println("Successfully subscribed!");

            while (true) {
                final Mqtt5Publish publish = publishes.receive();
                System.out.println("Received publish with payload: " + new String(publish.getPayloadAsBytes(), UTF_8));
            }
        }
    }
}
