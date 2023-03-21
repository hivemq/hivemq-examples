package com.hivemq.examples;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
public class MqttApplication {

    private final @NotNull Mqtt5Client mqttClient;

    public MqttApplication(final @NotNull String serverHost, final int serverPort) {
        mqttClient = Mqtt5Client.builder()
                .identifier("mqtt-application")
                .serverHost(serverHost)
                .serverPort(serverPort)
                .automaticReconnect()
                .initialDelay(1, TimeUnit.SECONDS) // 1
                .maxDelay(10, TimeUnit.SECONDS) // 1
                .applyAutomaticReconnect()
                .build();
    }

    public void start() {
        mqttClient.toBlocking().connectWith()
                .keepAlive(1) // 2
                .cleanStart(false) // 3
                .noSessionExpiry() // 3
                .send();
        final Flowable<Mqtt5Publish> publishFlowable = Flowable.intervalRange(0, 100, 0, 100, TimeUnit.MILLISECONDS)
                .map(aLong -> Mqtt5Publish.builder()
                        .topic("test/topic")
                        .payload(String.valueOf(aLong).getBytes(StandardCharsets.UTF_8))
                        .qos(MqttQos.EXACTLY_ONCE) // 4
                        .build()).onBackpressureBuffer();
        mqttClient.toRx().publish(publishFlowable).onBackpressureBuffer().subscribe();
    }
}
