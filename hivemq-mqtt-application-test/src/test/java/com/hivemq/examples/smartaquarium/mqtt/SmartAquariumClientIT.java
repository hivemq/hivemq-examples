package com.hivemq.examples.smartaquarium.mqtt;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.examples.smartaquarium.equipment.Co2;
import com.hivemq.examples.smartaquarium.equipment.Light;
import com.hivemq.examples.smartaquarium.equipment.Pump;
import com.hivemq.examples.smartaquarium.equipment.TemperatureSensor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@Testcontainers
class SmartAquariumClientIT {

    @Container
    @NotNull HiveMQContainer container = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"));

    private @NotNull Mqtt3BlockingClient testClient;

    @BeforeEach
    void setUp() {
        testClient = Mqtt3Client.builder()
                .serverPort(container.getMqttPort()).buildBlocking();
        testClient.connect();
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void test_lightTurnedOn() {

        final Light light = mock(Light.class);
        final Co2 co2 = mock(Co2.class);
        final Pump pump = mock(Pump.class);
        final TemperatureSensor temperatureSensor = mock(TemperatureSensor.class);

        final SmartAquariumClient smartAquariumClient = new SmartAquariumClient(
                container.getHost(),
                container.getMqttPort(),
                light,
                co2,
                pump,
                temperatureSensor);

        testClient.publishWith()
                .topic(SmartAquariumClient.LIGHT_TOPIC)
                .payload("ON".getBytes(StandardCharsets.UTF_8))
                .send();

        verify(light, timeout(30_000).times(1)).turnOn();
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void test_publishTemperatureBlocking() throws Exception {

        final Light light = mock(Light.class);
        final Co2 co2 = mock(Co2.class);
        final Pump pump = mock(Pump.class);
        final TemperatureSensor temperatureSensor = mock(TemperatureSensor.class);

        final SmartAquariumClient smartAquariumClient = new SmartAquariumClient(
                container.getHost(),
                container.getMqttPort(),
                light,
                co2,
                pump,
                temperatureSensor);

        testClient.subscribeWith()
                .topicFilter(SmartAquariumClient.TEMPERATURE_TOPIC)
                .qos(MqttQos.EXACTLY_ONCE)
                .send();

        final Mqtt3BlockingClient.Mqtt3Publishes publishes = testClient.publishes(MqttGlobalPublishFilter.ALL);

        when(temperatureSensor.getCelsius()).thenReturn(13.0f);
        smartAquariumClient.publishTemperature();

        final Mqtt3Publish mqtt3Publish = publishes.receive();

        assertNotNull(mqtt3Publish.getPayloadAsBytes());
        final String payload = new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
        assertEquals("13.0°C", payload);

    }
}