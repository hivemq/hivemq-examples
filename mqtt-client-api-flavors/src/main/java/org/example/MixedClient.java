package org.example;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yannick Weber
 */
public class MixedClient {
    public static void main(final String @NotNull [] args) {
        final var blockingClient = Mqtt5Client.builder()
                .identifier("mixed-client")
                .serverPort(1883)
                .serverHost("broker.hivemq.com")
                .buildBlocking();

        final var asyncClient = blockingClient.toAsync();
        final var rxClient = blockingClient.toRx();
    }
}
