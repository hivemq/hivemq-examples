package com.hivemq.examples.testcontainer.junit5;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MqttClientTestWithContainerExtension {

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension extension = new HiveMQTestContainerExtension();

    @Test
    void test_mqtt() {



        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverPort(extension.getMqttPort())
                .buildBlocking();

        client.connect();
        client.disconnect();
    }
}
